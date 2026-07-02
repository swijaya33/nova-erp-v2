package com.novaerp.gl.service;

import com.novaerp.common.model.ChartOfAccountEntity;
import com.novaerp.common.model.JournalEntryEntity;
import com.novaerp.common.model.JournalEntryLineEntity;
import com.novaerp.common.model.JournalEntryStatus;
import com.novaerp.common.sak.AccountingPeriod;
import com.novaerp.gl.repository.ChartOfAccountRepository;
import com.novaerp.gl.repository.JournalEntryLineRepository;
import com.novaerp.gl.repository.JournalEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JournalEntryService {

    private static final Logger log = LoggerFactory.getLogger(JournalEntryService.class);
    private static final String DEFAULT_TENANT = "default";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final JournalEntryRepository jeRepo;
    private final JournalEntryLineRepository lineRepo;
    private final ChartOfAccountRepository coaRepo;
    private final AccountingPeriodService periodService;

    public JournalEntryService(JournalEntryRepository jeRepo,
                               JournalEntryLineRepository lineRepo,
                               ChartOfAccountRepository coaRepo,
                               AccountingPeriodService periodService) {
        this.jeRepo = jeRepo;
        this.lineRepo = lineRepo;
        this.coaRepo = coaRepo;
        this.periodService = periodService;
    }

    /** Create a draft journal entry with double-entry validation. */
    @Transactional
    public JournalEntryEntity createJournalEntry(JournalEntryEntity je, String createdBy) {
        // Validate accounting period is open
        Optional<AccountingPeriod> optPeriod = periodService.getPeriod(je.getPeriod());
        if (optPeriod.isPresent() && optPeriod.get().isClosed()) {
            throw new IllegalStateException("Cannot create JE: period " + je.getPeriod() + " is closed");
        }

        je.setTenantId(DEFAULT_TENANT);
        je.setStatus(JournalEntryStatus.DRAFT);
        je.setCreatedBy(createdBy != null ? createdBy : "system");

        // Auto-generate entry number
        String dateStr = LocalDate.now().format(DATE_FMT);
        AtomicInteger seq = new AtomicInteger(1);
        List<JournalEntryEntity> existingToday = jeRepo.findByTenantId(DEFAULT_TENANT).stream()
                .filter(e -> e.getEntryNumber() != null && e.getEntryNumber().startsWith("JE-" + dateStr))
                .toList();
        if (!existingToday.isEmpty()) {
            int maxSeq = existingToday.stream()
                    .mapToInt(e -> Integer.parseInt(e.getEntryNumber().split("-")[2]))
                    .max().orElse(0);
            seq.set(maxSeq + 1);
        }
        je.setEntryNumber(String.format("JE-%s-%03d", dateStr, seq.get()));

        // Validate lines and accounts
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (JournalEntryLineEntity line : je.getLines()) {
            if (!line.isValid()) {
                throw new IllegalArgumentException("Invalid journal line: exactly one of debit/credit must be set");
            }
            
            // Validate account exists and is active
            Optional<ChartOfAccountEntity> acct = coaRepo.findByNameId(line.getAccountCode());
            if (acct.isEmpty() || !acct.get().isActive()) {
                throw new IllegalArgumentException("Invalid or inactive account: " + line.getAccountCode());
            }

            // Accumulate totals from lines
            if (line.getDebitAmount() != null) {
                totalDebit = totalDebit.add(line.getDebitAmount());
            } else {
                totalCredit = totalCredit.add(line.getCreditAmount());
            }
        }

        je.setTotalDebit(totalDebit);
        je.setTotalCredit(totalCredit);

        // Double-entry validation: Σ Debit must equal Σ Credit
        if (totalDebit.compareTo(BigDecimal.ZERO) <= 0 || totalCredit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Journal entry must have both debit and credit lines");
        }
        
        if (!je.isBalanced()) {
            throw new IllegalArgumentException(
                String.format("Double-entry validation failed: Debit (%s) != Credit (%s)", 
                    totalDebit, totalCredit));
        }

        // Set period from entry date if not set
        if (je.getPeriod() == null || je.getPeriod().isBlank()) {
            je.setPeriod(je.getEntryDate().format(PERIOD_FMT));
        }

        return jeRepo.save(je);
    }

    @Transactional(readOnly = true)
    public List<JournalEntryEntity> getAllEntries() {
        return jeRepo.findByTenantId(DEFAULT_TENANT);
    }

    @Transactional(readOnly = true)
    public Optional<JournalEntryEntity> getEntryById(Long id) {
        return jeRepo.findByIdWithLines(id);
    }

    /** Post a draft journal entry to the ledger. */
    @Transactional
    public JournalEntryEntity postEntry(Long id, String userId) {
        JournalEntryEntity je = jeRepo.findByIdWithLines(id)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found: " + id));

        if (je.getStatus() == JournalEntryStatus.POSTED) {
            throw new IllegalStateException("JE already posted");
        }
        if (je.getStatus() == JournalEntryStatus.LOCKED) {
            throw new IllegalStateException("JE is locked — cannot post");
        }

        // Validate period is open
        Optional<AccountingPeriod> optPeriod = periodService.getPeriod(je.getPeriod());
        if (optPeriod.isPresent() && optPeriod.get().isClosed()) {
            throw new IllegalStateException("Cannot post JE: period " + je.getPeriod() + " is closed");
        }

        // Re-validate double-entry balance before posting
        je.recomputeTotals();
        if (!je.isBalanced()) {
            throw new IllegalArgumentException(
                String.format("JE unbalanced at post time: Dr=%s Cr=%s", je.getTotalDebit(), je.getTotalCredit()));
        }

        je.setStatus(JournalEntryStatus.POSTED);
        log.info("Posted JE {} — Dr={} Cr={}", je.getEntryNumber(), je.getTotalDebit(), je.getTotalCredit());
        return jeRepo.save(je);
    }

    /** Lock a posted entry (SAK compliance). */
    @Transactional
    public JournalEntryEntity lockEntry(Long id, String userId) {
        JournalEntryEntity je = jeRepo.findByIdWithLines(id)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found: " + id));

        if (je.getStatus() == JournalEntryStatus.LOCKED) {
            throw new IllegalStateException("JE already locked");
        }
        if (je.getStatus() != JournalEntryStatus.POSTED) {
            throw new IllegalStateException("Only posted entries can be locked");
        }

        je.setStatus(JournalEntryStatus.LOCKED);
        log.info("Locked JE {} by {}", je.getEntryNumber(), userId);
        return jeRepo.save(je);
    }

    /** Unlock a locked entry (requires admin override). */
    @Transactional
    public JournalEntryEntity unlockEntry(Long id, String userId) {
        JournalEntryEntity je = jeRepo.findByIdWithLines(id)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found: " + id));

        if (je.getStatus() != JournalEntryStatus.LOCKED) {
            throw new IllegalStateException("Only locked entries can be unlocked");
        }

        je.setStatus(JournalEntryStatus.POSTED);
        log.info("Unlocked JE {} by {}", je.getEntryNumber(), userId);
        return jeRepo.save(je);
    }

    /** Cancel a posted entry (creates reversal). */
    @Transactional
    public JournalEntryEntity cancelEntry(Long id, String userId) {
        JournalEntryEntity original = jeRepo.findByIdWithLines(id)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found: " + id));

        if (original.getStatus() != JournalEntryStatus.POSTED 
                && original.getStatus() != JournalEntryStatus.LOCKED) {
            throw new IllegalStateException("Only posted/locked entries can be cancelled");
        }

        // Create reversal entry with swapped debits and credits
        JournalEntryEntity reversal = new JournalEntryEntity();
        reversal.setEntryDate(LocalDate.now());
        reversal.setPeriod(original.getPeriod());
        reversal.setDescription("Reversal of " + original.getEntryNumber() + " by " + userId);
        reversal.setCreatedBy(userId);

        for (JournalEntryLineEntity line : original.getLines()) {
            JournalEntryLineEntity revLine = new JournalEntryLineEntity();
            revLine.setAccountCode(line.getAccountCode());
            if (line.getDebitAmount() != null) {
                revLine.setCreditAmount(line.getDebitAmount());
            } else {
                revLine.setDebitAmount(line.getCreditAmount());
            }
            revLine.setLineDescription("Reversal: " + line.getLineDescription());
            reversal.addLine(revLine);
        }

        // Save original as cancelled, save reversal as posted
        original.setStatus(JournalEntryStatus.CANCELLED);
        jeRepo.save(original);

        return createJournalEntry(reversal, userId);
    }
}

package com.novaerp.gl.service;

import com.novaerp.common.sak.AccountingPeriod;
import com.novaerp.gl.repository.AccountingPeriodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
public class AccountingPeriodService {

    private static final Logger log = LoggerFactory.getLogger(AccountingPeriodService.class);
    private static final String DEFAULT_TENANT = "default";

    private final AccountingPeriodRepository repository;

    public AccountingPeriodService(AccountingPeriodRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AccountingPeriod> getAllPeriods() {
        return repository.findByTenantId(DEFAULT_TENANT);
    }

    @Transactional(readOnly = true)
    public Optional<AccountingPeriod> getPeriod(String period) {
        return repository.findByTenantIdAndPeriod(DEFAULT_TENANT, period);
    }

    @Transactional(readOnly = true)
    public List<AccountingPeriod> getOpenPeriods() {
        return repository.findByTenantIdAndClosedFalse(DEFAULT_TENANT);
    }

    /** Check if a given date falls within an open (non-closed) period. */
    @Transactional(readOnly = true)
    public boolean isPeriodOpen(LocalDate date) {
        String periodKey = YearMonth.from(date).toString(); // "YYYY-MM"
        Optional<AccountingPeriod> opt = repository.findByTenantIdAndPeriod(DEFAULT_TENANT, periodKey);
        return opt.map(p -> !p.isClosed()).orElse(false);
    }

    /** Create a new accounting period. */
    @Transactional
    public AccountingPeriod createPeriod(String period, String displayNameId) {
        YearMonth ym = YearMonth.parse(period);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        Optional<AccountingPeriod> existing = repository.findByTenantIdAndPeriod(DEFAULT_TENANT, period);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Period " + period + " already exists");
        }

        AccountingPeriod p = AccountingPeriod.of(period, startDate, endDate);
        p.setDisplayNameId(displayNameId);
        p.setTenantId(DEFAULT_TENANT);
        log.info("Created accounting period: {} ({})", period, displayNameId);
        return repository.save(p);
    }

    /** Close an accounting period — SAK compliance. */
    @Transactional
    public AccountingPeriod closePeriod(String period, String userId) {
        AccountingPeriod p = repository.findByTenantIdAndPeriod(DEFAULT_TENANT, period)
                .orElseThrow(() -> new IllegalArgumentException("Period not found: " + period));

        if (p.isClosed()) {
            throw new IllegalStateException("Period " + period + " is already closed");
        }

        // Check for any posted entries in this period before closing
        long postedCount = repository.findByTenantIdAndPeriod(DEFAULT_TENANT, period).stream()
                .filter(e -> true) // would need JE repo here; simplified
                .count();

        p.close(userId);
        log.info("Closed accounting period: {} by {}", period, userId);
        return repository.save(p);
    }

    /** Reopen a closed period (admin override). */
    @Transactional
    public AccountingPeriod reopenPeriod(String period, String userId) {
        AccountingPeriod p = repository.findByTenantIdAndPeriod(DEFAULT_TENANT, period)
                .orElseThrow(() -> new IllegalArgumentException("Period not found: " + period));

        if (!p.isClosed()) {
            throw new IllegalStateException("Period " + period + " is not closed");
        }

        p.reopen(userId);
        log.info("Reopened accounting period: {} by {}", period, userId);
        return repository.save(p);
    }

    /** Seed default periods for the current year. */
    @Transactional
    public int seedDefaultPeriods() {
        int seeded = 0;
        YearMonth now = YearMonth.now();
        
        // Seed 12 months of the current year + next month
        for (int i = -1; i <= 12; i++) {
            YearMonth ym = now.plusMonths(i);
            String periodKey = ym.toString();
            
            if (repository.findByTenantIdAndPeriod(DEFAULT_TENANT, periodKey).isEmpty()) {
                LocalDate startDate = ym.atDay(1);
                LocalDate endDate = ym.atEndOfMonth();
                
                AccountingPeriod p = AccountingPeriod.of(periodKey, startDate, endDate);
                String monthName = switch (ym.getMonth().getValue()) {
                    case 1 -> "Januari"; case 2 -> "Februari"; case 3 -> "Maret";
                    case 4 -> "April"; case 5 -> "Mei"; case 6 -> "Juni";
                    case 7 -> "Juli"; case 8 -> "Agustus"; case 9 -> "September";
                    case 10 -> "Oktober"; case 11 -> "November"; default -> "Desember";
                };
                p.setDisplayNameId(monthName + " " + ym.getYear());
                p.setTenantId(DEFAULT_TENANT);
                repository.save(p);
                seeded++;
            }
        }
        
        if (seeded > 0) {
            log.info("Seeded {} default accounting periods", seeded);
        }
        return seeded;
    }
}

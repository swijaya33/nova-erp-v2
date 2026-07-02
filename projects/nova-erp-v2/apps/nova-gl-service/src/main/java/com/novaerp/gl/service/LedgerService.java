package com.novaerp.gl.service;

import com.novaerp.common.model.JournalEntryEntity;
import com.novaerp.common.model.JournalEntryLineEntity;
import com.novaerp.common.model.JournalEntryStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ledger service — computes running balances per account from posted journal entries.
 */
@Service
public class LedgerService {

    private final JournalEntryService jeService;

    public LedgerService(JournalEntryService jeService) {
        this.jeService = jeService;
    }

    /** Calculate the running balance for a specific account up to a given date. */
    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(String accountCode, String period) {
        List<JournalEntryLineEntity> lines = jeService.getAllEntries().stream()
                .filter(je -> je.getStatus() == JournalEntryStatus.POSTED
                        || je.getStatus() == JournalEntryStatus.LOCKED)
                .flatMap(je -> je.getLines().stream())
                .filter(line -> line.getAccountCode().equals(accountCode))
                .toList();

        BigDecimal balance = BigDecimal.ZERO;
        for (JournalEntryLineEntity line : lines) {
            if (line.getDebitAmount() != null) {
                balance = balance.add(line.getDebitAmount());
            } else if (line.getCreditAmount() != null) {
                balance = balance.subtract(line.getCreditAmount());
            }
        }
        return balance;
    }

    /** Get running balances for all accounts in a period. */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getAllBalances(String period) {
        List<JournalEntryLineEntity> lines = jeService.getAllEntries().stream()
                .filter(je -> je.getStatus() == JournalEntryStatus.POSTED
                        || je.getStatus() == JournalEntryStatus.LOCKED)
                .flatMap(je -> je.getLines().stream())
                .toList();

        return lines.stream().collect(Collectors.groupingBy(
                JournalEntryLineEntity::getAccountCode,
                Collectors.reducing(BigDecimal.ZERO, line -> {
                    if (line.getDebitAmount() != null) return line.getDebitAmount();
                    return line.getCreditAmount().negate();
                }, BigDecimal::add)
        ));
    }

    /** Get running balance for a date range. */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getBalancesByDateRange(String startPeriod, String endPeriod) {
        List<JournalEntryLineEntity> lines = jeService.getAllEntries().stream()
                .filter(je -> je.getStatus() == JournalEntryStatus.POSTED
                        || je.getStatus() == JournalEntryStatus.LOCKED)
                .filter(je -> je.getPeriod() != null 
                        && je.getPeriod().compareTo(startPeriod) >= 0 
                        && je.getPeriod().compareTo(endPeriod) <= 0)
                .flatMap(je -> je.getLines().stream())
                .toList();

        return lines.stream().collect(Collectors.groupingBy(
                JournalEntryLineEntity::getAccountCode,
                Collectors.reducing(BigDecimal.ZERO, line -> {
                    if (line.getDebitAmount() != null) return line.getDebitAmount();
                    return line.getCreditAmount().negate();
                }, BigDecimal::add)
        ));
    }
}

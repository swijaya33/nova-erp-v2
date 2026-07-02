package com.novaerp.gl.service;

import com.novaerp.common.model.ChartOfAccountEntity;
import com.novaerp.common.model.JournalEntryEntity;
import com.novaerp.common.model.JournalEntryStatus;
import com.novaerp.common.sak.AccountCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final JournalEntryService jeService;
    private final ChartOfAccountService coaService;

    public ReportService(JournalEntryService jeService, ChartOfAccountService coaService) {
        this.jeService = jeService;
        this.coaService = coaService;
    }

    // ================================================================
    // Trial Balance Report
    // ================================================================

    /** Sum all debits and credits per account for a given period. */
    @Transactional(readOnly = true)
    public Map<String, TrialBalanceReport> getTrialBalance(String period) {
        List<JournalEntryEntity> entries = jeService.getAllEntries().stream()
                .filter(je -> je.getStatus() == JournalEntryStatus.POSTED
                        || je.getStatus() == JournalEntryStatus.LOCKED)
                .filter(je -> period.equals(je.getPeriod()))
                .toList();

        Map<String, TrialBalanceReport> result = new LinkedHashMap<>();

        // Initialize all accounts with zero balances
        List<ChartOfAccountEntity> allAccounts = coaService.getAllAccounts();
        for (ChartOfAccountEntity acct : allAccounts) {
            result.put(acct.getAccountCode(), 
                    new TrialBalanceReport(acct.getAccountCode(), acct.getAccountNameId()));
        }

        // Accumulate debits and credits per account from journal lines
        for (JournalEntryEntity je : entries) {
            for (var line : je.getLines()) {
                String code = line.getAccountCode();
                TrialBalanceReport report = result.computeIfAbsent(code, 
                        k -> new TrialBalanceReport(k, "Unknown"));
                
                if (line.getDebitAmount() != null) {
                    report.debitTotal = report.debitTotal.add(line.getDebitAmount());
                }
                if (line.getCreditAmount() != null) {
                    report.creditTotal = report.creditTotal.add(line.getCreditAmount());
                }
            }
        }

        return result;
    }

    /** Get the grand totals for a trial balance. */
    public TrialBalanceTotals getTrialBalanceTotals(String period) {
        Map<String, TrialBalanceReport> tb = getTrialBalance(period);
        
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (TrialBalanceReport r : tb.values()) {
            totalDebit = totalDebit.add(r.debitTotal);
            totalCredit = totalCredit.add(r.creditTotal);
        }

        return new TrialBalanceTotals(totalDebit, totalCredit, 
                totalDebit.compareTo(totalCredit) == 0 ? "BALANCED" : "UNBALANCED");
    }

    // ================================================================
    // Income Statement (Laba Rugi)
    // ================================================================

    /** Revenue minus expenses for a period. */
    @Transactional(readOnly = true)
    public IncomeStatement getIncomeStatement(String period) {
        Map<String, TrialBalanceReport> tb = getTrialBalance(period);
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        List<IncomeLineItem> revenueItems = new ArrayList<>();
        List<IncomeLineItem> expenseItems = new ArrayList<>();

        for (var entry : tb.entrySet()) {
            String code = entry.getKey();
            TrialBalanceReport report = entry.getValue();
            
            // Net amount: credit - debit (revenue accounts normally have credit balance)
            BigDecimal netAmount = report.creditTotal.subtract(report.debitTotal);
            
            if (!netAmount.equals(BigDecimal.ZERO)) {
                IncomeLineItem item = new IncomeLineItem(code, report.accountName, netAmount.abs(), 
                        report.creditTotal.compareTo(report.debitTotal) > 0 ? "CREDIT" : "DEBIT");

                // Classify by account code prefix
                if (code.startsWith("4")) {
                    totalRevenue = totalRevenue.add(netAmount);
                    revenueItems.add(item);
                } else if (code.startsWith("5") || code.startsWith("6") 
                        || code.startsWith("7") || code.startsWith("8")) {
                    totalExpenses = totalExpenses.add(netAmount);
                    expenseItems.add(item);
                }
            }
        }

        BigDecimal netIncome = totalRevenue.subtract(totalExpenses);

        return new IncomeStatement(period, revenueItems, totalRevenue, 
                                   expenseItems, totalExpenses, netIncome);
    }

    // ================================================================
    // Balance Sheet (Neraca)
    // ================================================================

    /** Assets = Liabilities + Equity snapshot as of a date. */
    @Transactional(readOnly = true)
    public BalanceSheet getBalanceSheet(String period) {
        Map<String, TrialBalanceReport> tb = getTrialBalance(period);
        
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        List<BalanceLineItem> assetItems = new ArrayList<>();
        List<BalanceLineItem> liabilityItems = new ArrayList<>();
        List<BalanceLineItem> equityItems = new ArrayList<>();

        for (var entry : tb.entrySet()) {
            String code = entry.getKey();
            TrialBalanceReport report = entry.getValue();
            
            // Net balance: debit - credit (asset accounts normally have debit balance)
            BigDecimal netAmount = report.debitTotal.subtract(report.creditTotal);
            
            if (!netAmount.equals(BigDecimal.ZERO)) {
                BalanceLineItem item = new BalanceLineItem(code, report.accountName, 
                        netAmount.abs(), netAmount.compareTo(BigDecimal.ZERO) > 0 ? "DEBIT" : "CREDIT");

                // Classify by account code prefix
                if (code.startsWith("1")) {
                    totalAssets = totalAssets.add(netAmount);
                    assetItems.add(item);
                } else if (code.startsWith("2")) {
                    totalLiabilities = totalLiabilities.add(netAmount);
                    liabilityItems.add(item);
                } else if (code.startsWith("3")) {
                    totalEquity = totalEquity.add(netAmount);
                    equityItems.add(item);
                }
            }
        }

        BigDecimal totalLiabilityAndEquity = totalLiabilities.add(totalEquity);

        return new BalanceSheet(period, assetItems, totalAssets, 
                                liabilityItems, totalLiabilities, 
                                equityItems, totalEquity, 
                                totalLiabilityAndEquity);
    }

    // ================================================================
    // Report Data Classes
    // ================================================================

    public static class TrialBalanceReport {
        private final String accountCode;
        private final String accountName;
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;
        
        public TrialBalanceReport(String accountCode, String accountName) {
            this.accountCode = accountCode;
            this.accountName = accountName;
        }
        
        public BigDecimal getNetAmount() { return creditTotal.subtract(debitTotal); }
        public boolean isDebitNormal() { return debitTotal.compareTo(creditTotal) > 0; }
    }

    public record TrialBalanceTotals(BigDecimal totalDebit, BigDecimal totalCredit, String status) {}

    public record IncomeLineItem(String accountCode, String accountName, 
                                  BigDecimal amount, String balanceType) {}

    public record IncomeStatement(String period, List<IncomeLineItem> revenueItems,
                                   BigDecimal totalRevenue,
                                   List<IncomeLineItem> expenseItems,
                                   BigDecimal totalExpenses,
                                   BigDecimal netIncome) {
        public boolean isProfit() { return netIncome.compareTo(BigDecimal.ZERO) > 0; }
    }

    public record BalanceLineItem(String accountCode, String accountName,
                                   BigDecimal amount, String balanceType) {}

    public record BalanceSheet(String period, List<BalanceLineItem> assetItems,
                                BigDecimal totalAssets,
                                List<BalanceLineItem> liabilityItems,
                                BigDecimal totalLiabilities,
                                List<BalanceLineItem> equityItems,
                                BigDecimal totalEquity,
                                BigDecimal totalLiabilityAndEquity) {
        public boolean isBalanced() { 
            return totalAssets.compareTo(totalLiabilityAndEquity) == 0; 
        }
    }
}

package com.novaerp.common.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * A single line in a double-entry journal entry.
 * Exactly one of debitAmount or creditAmount is non-null per line.
 */
@Entity
@Table(name = "gl_journal_lines")
public class JournalEntryLineEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntryEntity journalEntry;

    /** Account code being debited or credited */
    @Column(name = "account_code", nullable = false, length = 10)
    private String accountCode;

    /** Debit amount — exactly one of debitAmount/creditAmount must be non-null */
    @Column(name = "debit_amount", precision = 20, scale = 2)
    private BigDecimal debitAmount;

    /** Credit amount — exactly one of debitAmount/creditAmount must be non-null */
    @Column(name = "credit_amount", precision = 20, scale = 2)
    private BigDecimal creditAmount;

    /** Line-level memo/description */
    @Column(name = "line_description", length = 500)
    private String lineDescription;

    public JournalEntryLineEntity() {}

    public static JournalEntryLineEntity debit(String accountCode, BigDecimal amount, String description) {
        JournalEntryLineEntity line = new JournalEntryLineEntity();
        line.accountCode = accountCode;
        line.debitAmount = amount;
        line.creditAmount = null;
        line.lineDescription = description;
        return line;
    }

    public static JournalEntryLineEntity credit(String accountCode, BigDecimal amount, String description) {
        JournalEntryLineEntity line = new JournalEntryLineEntity();
        line.accountCode = accountCode;
        line.debitAmount = null;
        line.creditAmount = amount;
        line.lineDescription = description;
        return line;
    }

    /** Validate: exactly one of debit/credit must be non-null */
    public boolean isValid() {
        boolean hasDebit = debitAmount != null && debitAmount.compareTo(BigDecimal.ZERO) > 0;
        boolean hasCredit = creditAmount != null && creditAmount.compareTo(BigDecimal.ZERO) > 0;
        return (hasDebit ^ hasCredit); // XOR — exactly one must be true
    }

    /** Get the non-null amount */
    public BigDecimal getAmount() {
        return debitAmount != null ? debitAmount : creditAmount;
    }

    /** Is this line a debit? */
    public boolean isDebitLine() { return debitAmount != null && debitAmount.compareTo(BigDecimal.ZERO) > 0; }

    // --- Getters/Setters ---

    public JournalEntryEntity getJournalEntry() { return journalEntry; }
    public void setJournalEntry(JournalEntryEntity je) { this.journalEntry = je; }
    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public BigDecimal getDebitAmount() { return debitAmount; }
    public void setDebitAmount(BigDecimal debitAmount) { this.debitAmount = debitAmount; }
    public BigDecimal getCreditAmount() { return creditAmount; }
    public void setCreditAmount(BigDecimal creditAmount) { this.creditAmount = creditAmount; }
    public String getLineDescription() { return lineDescription; }
    public void setLineDescription(String lineDescription) { this.lineDescription = lineDescription; }

    @Override
    public String toString() {
        String indent = isDebitLine() ? "  Dr: " : "      Cr: ";
        return accountCode + indent + getAmount();
    }
}

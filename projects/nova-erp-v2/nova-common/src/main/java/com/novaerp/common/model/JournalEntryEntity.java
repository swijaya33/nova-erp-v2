package com.novaerp.common.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SAK double-entry journal entry.
 * Enforces Σ Debit = Σ Credit via JournalEntryLineEntity child rows.
 */
@Entity
@Table(name = "gl_journal_entries", indexes = {
    @Index(name = "idx_gl_je_tenant_period", columnList = "tenantId,period"),
    @Index(name = "idx_gl_je_number", columnList = "entryNumber", unique = true)
})
public class JournalEntryEntity extends BaseEntity {

    @Column(name = "entry_number", nullable = false, length = 32)
    private String entryNumber; // Auto-generated: JE-YYYYMMDD-NNN

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    /** Accounting period this entry belongs to (e.g., "2026-07") */
    @Column(name = "period", nullable = false, length = 7)
    private String period; // YYYY-MM format

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private JournalEntryStatus status;

    /** Total debit amount (sum of all lines' debit) */
    @Column(name = "total_debit", precision = 20, scale = 2)
    private BigDecimal totalDebit;

    /** Total credit amount (sum of all lines' credit) — must equal totalDebit */
    @Column(name = "total_credit", precision = 20, scale = 2)
    private BigDecimal totalCredit;

    @Column(name = "description", length = 500)
    private String description;

    /** Who created this entry (user ID from JWT) */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<JournalEntryLineEntity> lines = new java.util.ArrayList<>();

    public JournalEntryEntity() {}

    // --- SAK double-entry validation ---

    /** Validate that Σ Debit = Σ Credit */
    public boolean isBalanced() {
        if (totalDebit == null || totalCredit == null) return false;
        return totalDebit.compareTo(totalCredit) == 0 && totalDebit.compareTo(BigDecimal.ZERO) > 0;
    }

    /** Recompute totals from lines */
    public void recomputeTotals() {
        this.totalDebit = BigDecimal.ZERO;
        this.totalCredit = BigDecimal.ZERO;
        for (JournalEntryLineEntity line : lines) {
            if (line.getDebitAmount() != null) {
                this.totalDebit = this.totalDebit.add(line.getDebitAmount());
            }
            if (line.getCreditAmount() != null) {
                this.totalCredit = this.totalCredit.add(line.getCreditAmount());
            }
        }
    }

    /** Add a line to this entry */
    public void addLine(JournalEntryLineEntity line) {
        lines.add(line);
        line.setJournalEntry(this);
        recomputeTotals();
    }

    // --- Getters/Setters ---

    public String getEntryNumber() { return entryNumber; }
    public void setEntryNumber(String entryNumber) { this.entryNumber = entryNumber; }
    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public JournalEntryStatus getStatus() { return status; }
    public void setStatus(JournalEntryStatus status) { this.status = status; }
    public BigDecimal getTotalDebit() { return totalDebit; }
    public BigDecimal getTotalCredit() { return totalCredit; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public java.util.List<JournalEntryLineEntity> getLines() { return lines; }

    /** Set total debit (used by services during JE creation). */
    public void setTotalDebit(BigDecimal totalDebit) { this.totalDebit = totalDebit; }

    /** Set total credit (used by services during JE creation). */
    public void setTotalCredit(BigDecimal totalCredit) { this.totalCredit = totalCredit; }

    @Override
    public String toString() {
        return "JE[" + entryNumber + "] " + period + " Dr=" + totalDebit + " Cr=" + totalCredit 
             + (isBalanced() ? " balanced" : " UNBALANCED");
    }
}

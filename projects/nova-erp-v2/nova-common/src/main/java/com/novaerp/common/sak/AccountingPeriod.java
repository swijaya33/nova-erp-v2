package com.novaerp.common.sak;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * SAK accounting period management.
 * Supports monthly periods with open/close lock mechanism per SAK audit trail requirements.
 */
@Entity
@Table(name = "gl_accounting_periods", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenantId", "period"})
})
public class AccountingPeriod extends com.novaerp.common.model.BaseEntity {

    /** Period identifier in YYYY-MM format (e.g., "2026-07") */
    @Column(name = "period", nullable = false, length = 7)
    private String period;

    /** Display name (e.g., "Juli 2026") */
    @Column(name = "display_name_id", length = 100)
    private String displayNameId;

    @Column(name = "display_name_en", length = 100)
    private String displayNameEn;

    /** Period start date (first day of month) */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Period end date (last day of month) */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Whether this period is closed — no journal entries can be modified/created */
    @Column(name = "is_closed", nullable = false)
    private boolean closed = false;

    /** Who closed this period (user ID) */
    @Column(name = "closed_by", length = 64)
    private String closedBy;

    /** When the period was closed */
    @Column(name = "closed_at")
    private java.time.LocalDateTime closedAt;

    public AccountingPeriod() {}

    public static AccountingPeriod of(String period, LocalDate startDate, LocalDate endDate) {
        AccountingPeriod p = new AccountingPeriod();
        p.period = period;
        p.startDate = startDate;
        p.endDate = endDate;
        return p;
    }

    /** Close this period — SAK compliance: once closed, no modifications allowed */
    public void close(String userId) {
        if (this.closed) throw new IllegalStateException("Period " + period + " is already closed");
        this.closed = true;
        this.closedBy = userId;
        this.closedAt = java.time.LocalDateTime.now();
    }

    /** Reopen a closed period — requires admin override */
    public void reopen(String userId) {
        if (!this.closed) throw new IllegalStateException("Period " + period + " is not closed");
        this.closed = false;
        this.closedBy = null;
        this.closedAt = null;
    }

    // --- Getters/Setters ---

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getDisplayNameId() { return displayNameId; }
    public void setDisplayNameId(String name) { this.displayNameId = name; }
    public String getDisplayNameEn() { return displayNameEn; }
    public void setDisplayNameEn(String name) { this.displayNameEn = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate date) { this.startDate = date; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate date) { this.endDate = date; }
    public boolean isClosed() { return closed; }
    public String getClosedBy() { return closedBy; }
    public java.time.LocalDateTime getClosedAt() { return closedAt; }

    @Override
    public String toString() {
        return period + (closed ? " [LOCKED]" : " [OPEN]") 
             + (displayNameId != null ? " - " + displayNameId : "");
    }
}

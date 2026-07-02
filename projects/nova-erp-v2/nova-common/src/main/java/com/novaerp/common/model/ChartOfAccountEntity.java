package com.novaerp.common.model;

import com.novaerp.common.sak.AccountCategory;
import jakarta.persistence.*;

/**
 * SAK-aligned Chart of Accounts entity.
 * Every Nova ERP GL service uses this as the base CoA model.
 */
@Entity
@Table(name = "gl_accounts", indexes = {
    @Index(name = "idx_gl_accounts_tenant_code", columnList = "tenantId,accountCode", unique = true),
    @Index(name = "idx_gl_accounts_category", columnList = "category")
})
public class ChartOfAccountEntity extends BaseEntity {

    @Column(name = "account_code", nullable = false, length = 10)
    private String accountCode;

    @Column(name = "account_name_id", nullable = false, length = 200)
    private String accountNameId; // Indonesian name

    @Column(name = "account_name_en", length = 200)
    private String accountNameEn; // English name (optional)

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 16)
    private AccountCategory category;

    /** DEBIT or CREDIT — the normal balance direction for this account */
    @Column(name = "normal_balance", nullable = false, length = 6)
    private String normalBalance; // "DEBIT" or "CREDIT"

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "parent_code", length = 10)
    private String parentCode; // For hierarchical CoA (e.g., 1000 → 1010, 1020)

    @Column(name = "description", length = 500)
    private String description;

    public ChartOfAccountEntity() {}

    public ChartOfAccountEntity(String accountCode, String nameId, AccountCategory category, 
                                 String normalBalance, String parentCode) {
        this.accountCode = accountCode;
        this.accountNameId = nameId;
        this.category = category;
        this.normalBalance = normalBalance;
        this.parentCode = parentCode;
    }

    // --- SAK validation helpers ---

    /** Validate that the code matches the expected SAK prefix for its category */
    public boolean hasValidSakPrefix() {
        return category != null && category.isValidCode(accountCode);
    }

    /** Get display name in preferred locale */
    public String getDisplayName(String locale) {
        if ("id".equalsIgnoreCase(locale)) {
            return accountNameId;
        }
        return accountNameEn != null ? accountNameEn : accountNameId;
    }

    // --- Getters/Setters ---

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public String getAccountNameId() { return accountNameId; }
    public void setAccountNameId(String nameId) { this.accountNameId = nameId; }
    public String getAccountNameEn() { return accountNameEn; }
    public void setAccountNameEn(String nameEn) { this.accountNameEn = nameEn; }
    public AccountCategory getCategory() { return category; }
    public void setCategory(AccountCategory category) { this.category = category; }
    public String getNormalBalance() { return normalBalance; }
    public void setNormalBalance(String normalBalance) { this.normalBalance = normalBalance; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return accountCode + " - " + accountNameId + " (" + category.getNameId() + ")";
    }
}

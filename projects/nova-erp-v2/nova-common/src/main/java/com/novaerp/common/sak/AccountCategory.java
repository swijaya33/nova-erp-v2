package com.novaerp.common.sak;

/**
 * SAK (Standar Akuntansi Keuangan) account categories.
 * Standard 5-category classification required by OJK/financial reporting norms in Indonesia.
 * Account codes follow: 1xxx=Harta(Asset), 2xxx=Kewajiban(Liability), 3xxx=Ekuitas(Equity),
 * 4xxx=Pendapatan(Revenue), 5xxx+=Beban(Expense).
 */
public enum AccountCategory {

    /** Harta (Assets) — current + non-current */
    HARTA("1", "Harta", "Asset"),

    /** Kewajiban (Liabilities) — current + non-current */
    KEWAJIBAN("2", "Kewajiban", "Liability"),

    /** Ekuitas (Equity) */
    EKUITAS("3", "Ekuitas", "Equity"),

    /** Pendapatan (Revenue) */
    PENDAPATAN("4", "Pendapatan", "Revenue"),

    /** Beban (Expenses) */
    BEBAN("5", "Beban", "Expense");

    private final String prefix;
    private final String nameId;  // Indonesian name
    private final String nameEn;  // English name

    AccountCategory(String prefix, String nameId, String nameEn) {
        this.prefix = prefix;
        this.nameId = nameId;
        this.nameEn = nameEn;
    }

    public String getPrefix() { return prefix; }
    public String getNameId() { return nameId; }
    public String getNameEn() { return nameEn; }

    /** Validate that an account code matches this category's SAK prefix */
    public boolean isValidCode(String accountCode) {
        if (accountCode == null || accountCode.length() < 4) return false;
        return accountCode.startsWith(prefix);
    }

    /** Parse from string (case-insensitive) */
    public static AccountCategory fromString(String s) {
        for (AccountCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(s)) return cat;
        }
        throw new IllegalArgumentException("Unknown account category: " + s);
    }

    /** Parse prefix to category (e.g., "1" → HARTA) */
    public static AccountCategory fromPrefix(String prefix) {
        for (AccountCategory cat : values()) {
            if (cat.prefix.equals(prefix)) return cat;
        }
        throw new IllegalArgumentException("Unknown SAK prefix: " + prefix);
    }
}

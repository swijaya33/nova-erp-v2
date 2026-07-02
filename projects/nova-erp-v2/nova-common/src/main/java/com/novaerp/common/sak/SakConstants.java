package com.novaerp.common.sak;

import java.util.*;

/**
 * SAK (Standar Akuntansi Keuangan) standard seed data for Chart of Accounts.
 * Pre-seeds ≥30 accounts following Indonesian SMB accounting conventions.
 * 
 * Account code structure:
 *   1xxx = Harta (Assets):     11xx=Harta Lancar, 12xx=Harta Tidak Lancar
 *   2xxx = Kewajiban (Liabilities): 21xx=Kewajiban Lancar, 22xx=Kewajiban Tidak Lancar
 *   3xxx = Ekuitas (Equity)
 *   4xxx = Pendapatan (Revenue):    41xx=Pendapatan Usaha, 42xx=Pendapatan Lain
 *   5xxx = Beban (Expenses):        51xx=Beban Usaha, 52xx=Beban Lainnya
 */
public final class SakConstants {

    private SakConstants() {}

    /** Standard seed accounts — each entry: [code, nameId, nameEn, category, normalBalance] */
    public static List<SeedAccount> getStandardSeedAccounts() {
        return List.of(
            // === HARTA (Assets) 1xxx ===
            new SeedAccount("1000", "Kas", "Cash", AccountCategory.HARTA, "DEBIT"),
            new SeedAccount("1010", "Bank BCA", "BCA Bank", AccountCategory.HARTA, "DEBIT"),
            new SeedAccount("1020", "Bank Mandiri", "Mandiri Bank", AccountCategory.HARTA, "DEBIT"),
            new SeedAccount("1100", "Piutang Usaha", "Trade Receivables", AccountCategory.HARTA, "DEBIT"),
            new SeedAccount("1110", "Piutang Non-Kas", "Non-Cash Receivables", AccountCategory.HARTA, "DEBIT"),
            new SeedAccount("1200", "Persediaan Barang Dagang", "Merchandise Inventory", AccountCategory.HARTA, "DEBIT"),
            new SeedAccount("1210", "Persediaan Bahan Baku", "Raw Materials", AccountCategory.HARTA, "DEBIT"),
            new SeedAccount("1300", "Perlengkapan", "Supplies", AccountCategory.HARTA, "DEBIT"),
            new SeedAccount("1400", "Pembayaran Dimuka", "Prepaid Expenses", AccountCategory.HARTA, "DEBIT"),

            // === Harta Tidak Lancar (Non-Current Assets) 15xx+ ===
            new SeedAccount("1500", "Aset Tetap", "Fixed Assets", AccountCategory.HARTA, "DEBIT"),
            new SeedAccount("1510", "Akumulasi Penyusutan Aset Tetap", "Accumulated Depreciation", AccountCategory.HARTA, "CREDIT"),
            new SeedAccount("1600", "Aset Tidak Berwujud", "Intangible Assets", AccountCategory.HARTA, "DEBIT"),

            // === KEWAJIBAN (Liabilities) 2xxx ===
            new SeedAccount("2000", "Utang Usaha", "Trade Payables", AccountCategory.KEWAJIBAN, "CREDIT"),
            new SeedAccount("2100", "Utang Gaji", "Salaries Payable", AccountCategory.KEWAJIBAN, "CREDIT"),
            new SeedAccount("2101", "Hutang Usaha", "Trade Payables (SAK)", AccountCategory.KEWAJIBAN, "CREDIT"),
            new SeedAccount("2102", "Hutang Gaji", "Salaries Payable (SAK)", AccountCategory.KEWAJIBAN, "CREDIT"),
            new SeedAccount("2103", "PPN Keluaran", "Output VAT (PPN)", AccountCategory.KEWAJIBAN, "CREDIT"),
            new SeedAccount("2104", "GRNI Clearing", "GRNI Clearing", AccountCategory.KEWAJIBAN, "CREDIT"),
            new SeedAccount("2200", "Utang Pajak", "Tax Payables", AccountCategory.KEWAJIBAN, "CREDIT"),
            new SeedAccount("2201", "Pinjaman Bank Jangka Panjang", "Long-term Bank Loan", AccountCategory.KEWAJIBAN, "CREDIT"),

            // === EKUITAS (Equity) 3xxx ===
            new SeedAccount("3000", "Modal Disetor", "Paid-in Capital", AccountCategory.EKUITAS, "CREDIT"),
            new SeedAccount("3100", "Laba Ditahan", "Retained Earnings", AccountCategory.EKUITAS, "CREDIT"),
            new SeedAccount("3200", "Laba Rugi Tahun Berjalan", "Current Year P&L", AccountCategory.EKUITAS, "CREDIT"),

            // === PENDAPATAN (Revenue) 4xxx ===
            new SeedAccount("4000", "Pendapatan Penjualan", "Sales Revenue", AccountCategory.PENDAPATAN, "CREDIT"),
            new SeedAccount("4100", "Pendapatan Jasa", "Service Revenue", AccountCategory.PENDAPATAN, "CREDIT"),
            new SeedAccount("4200", "Pendapatan Lain-lain", "Other Income", AccountCategory.PENDAPATAN, "CREDIT"),
            new SeedAccount("4300", "Potongan Penjualan", "Sales Discounts", AccountCategory.PENDAPATAN, "DEBIT"),

            // === BEBAN (Expenses) 5xxx+ ===
            new SeedAccount("5000", "Beban Pokok Penjualan", "Cost of Goods Sold", AccountCategory.BEBAN, "DEBIT"),
            new SeedAccount("5100", "Beban Gaji", "Salary Expense", AccountCategory.BEBAN, "DEBIT"),
            new SeedAccount("5200", "Beban Sewa", "Rent Expense", AccountCategory.BEBAN, "DEBIT"),
            new SeedAccount("5300", "Beban Listrik dan Air", "Utilities Expense", AccountCategory.BEBAN, "DEBIT"),
            new SeedAccount("5400", "Beban Penyusutan", "Depreciation Expense", AccountCategory.BEBAN, "DEBIT"),
            new SeedAccount("5500", "Beban Administrasi", "Administrative Expense", AccountCategory.BEBAN, "DEBIT"),
            new SeedAccount("5600", "Beban Pemasaran", "Marketing Expense", AccountCategory.BEBAN, "DEBIT"),
            new SeedAccount("5700", "Beban Pajak Pajak", "Tax Expense", AccountCategory.BEBAN, "DEBIT"),
            new SeedAccount("5800", "Beban Bunga", "Interest Expense", AccountCategory.BEBAN, "DEBIT"),
            new SeedAccount("5900", "Beban Lain-lain", "Other Expenses", AccountCategory.BEBAN, "DEBIT")
        );
    }

    /** SAK account code validation: must be 4 digits starting with valid prefix */
    public static boolean isValidSakCode(String code) {
        if (code == null || !code.matches("\\d{4}")) return false;
        char first = code.charAt(0);
        return "12345".indexOf(first) >= 0;
    }

    /** Get category from account code prefix */
    public static AccountCategory getCategoryFromCode(String code) {
        if (!isValidSakCode(code)) throw new IllegalArgumentException("Invalid SAK code: " + code);
        return AccountCategory.fromPrefix(String.valueOf(code.charAt(0)));
    }

    public record SeedAccount(String code, String nameId, String nameEn, 
                               AccountCategory category, String normalBalance) {}
}

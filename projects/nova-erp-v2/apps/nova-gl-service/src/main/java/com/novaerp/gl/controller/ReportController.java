package com.novaerp.gl.controller;

import com.novaerp.gl.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gl/reports")
@Tag(name = "GL Reports", description = "SAK-compliant financial reports: Trial Balance, Income Statement (Laba Rugi), Balance Sheet (Neraca)")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/trial-balance")
    @Operation(summary = "Trial balance for a period (e.g., ?period=2026-07)")
    public ResponseEntity<ReportService.TrialBalanceTotals> trialBalance(@RequestParam String period) {
        return ResponseEntity.ok(reportService.getTrialBalanceTotals(period));
    }

    @GetMapping("/income-statement")
    @Operation(summary = "Income Statement / Laba Rugi for a period (e.g., ?period=2026-07)")
    public ResponseEntity<ReportService.IncomeStatement> incomeStatement(@RequestParam String period) {
        return ResponseEntity.ok(reportService.getIncomeStatement(period));
    }

    @GetMapping("/balance-sheet")
    @Operation(summary = "Balance Sheet / Neraca as of a date (e.g., ?date=2026-07-31)")
    public ResponseEntity<ReportService.BalanceSheet> balanceSheet(@RequestParam String date) {
        // Convert YYYY-MM-DD to YYYY-MM period
        String period = date.substring(0, 7);
        return ResponseEntity.ok(reportService.getBalanceSheet(period));
    }
}

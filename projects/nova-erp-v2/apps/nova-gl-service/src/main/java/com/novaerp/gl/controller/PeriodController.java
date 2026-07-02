package com.novaerp.gl.controller;

import com.novaerp.common.sak.AccountingPeriod;
import com.novaerp.gl.service.AccountingPeriodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gl/periods")
@Tag(name = "Accounting Periods", description = "SAK-compliant accounting period management with open/close lock mechanism")
public class PeriodController {

    private final AccountingPeriodService service;

    public PeriodController(AccountingPeriodService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all accounting periods")
    public ResponseEntity<List<AccountingPeriod>> getAll() {
        return ResponseEntity.ok(service.getAllPeriods());
    }

    @PostMapping("/close")
    @Operation(summary = "Close an accounting period (SAK compliance)")
    public ResponseEntity<AccountingPeriod> close(@RequestParam String period,
                                                    @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.closePeriod(period, userId != null ? userId : "system"));
    }

    @PostMapping("/reopen")
    @Operation(summary = "Reopen a closed accounting period (admin override)")
    public ResponseEntity<AccountingPeriod> reopen(@RequestParam String period,
                                                    @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.reopenPeriod(period, userId != null ? userId : "system"));
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new accounting period")
    public ResponseEntity<AccountingPeriod> create(@RequestParam String period,
                                                    @RequestParam(required = false) String displayNameId) {
        return ResponseEntity.ok(service.createPeriod(period, displayNameId != null ? displayNameId : period));
    }

    @GetMapping("/open-periods")
    @Operation(summary = "List all open (non-closed) periods")
    public ResponseEntity<List<AccountingPeriod>> getOpen() {
        return ResponseEntity.ok(service.getOpenPeriods());
    }
}

package com.novaerp.gl.controller;

import com.novaerp.common.model.ChartOfAccountEntity;
import com.novaerp.gl.service.ChartOfAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gl/accounts")
@Tag(name = "Chart of Accounts", description = "SAK-compliant chart of accounts management")
public class ChartOfAccountController {

    private final ChartOfAccountService service;

    public ChartOfAccountController(ChartOfAccountService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all active accounts")
    public ResponseEntity<List<ChartOfAccountEntity>> getAll() {
        return ResponseEntity.ok(service.getAllAccounts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<ChartOfAccountEntity> getById(@PathVariable Long id) {
        return service.getAccountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<ChartOfAccountEntity> create(@RequestBody ChartOfAccountEntity entity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing account")
    public ResponseEntity<ChartOfAccountEntity> update(@PathVariable Long id, 
                                                        @RequestBody ChartOfAccountEntity entity) {
        return ResponseEntity.ok(service.updateAccount(id, entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete an account (set inactive)")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        service.softDeleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/seed")
    @Operation(summary = "Seed SAK standard accounts if table is empty")
    public ResponseEntity<String> seed() {
        int count = service.seedSakAccounts();
        return ResponseEntity.ok("Seeded " + count + " SAK accounts");
    }
}

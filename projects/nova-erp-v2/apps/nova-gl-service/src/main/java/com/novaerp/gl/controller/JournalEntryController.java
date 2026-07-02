package com.novaerp.gl.controller;

import com.novaerp.common.model.JournalEntryEntity;
import com.novaerp.gl.service.JournalEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gl/journal-entries")
@Tag(name = "Journal Entries", description = "SAK double-entry journal entry management")
public class JournalEntryController {

    private final JournalEntryService service;

    public JournalEntryController(JournalEntryService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a draft journal entry (double-entry validated)")
    public ResponseEntity<JournalEntryEntity> create(@RequestBody JournalEntryEntity entity,
                                                      @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createJournalEntry(entity, userId));
    }

    @GetMapping
    @Operation(summary = "List all journal entries")
    public ResponseEntity<List<JournalEntryEntity>> getAll() {
        return ResponseEntity.ok(service.getAllEntries());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get journal entry by ID (with lines)")
    public ResponseEntity<JournalEntryEntity> getById(@PathVariable Long id) {
        return service.getEntryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/post")
    @Operation(summary = "Post a draft journal entry to the ledger")
    public ResponseEntity<JournalEntryEntity> post(@PathVariable Long id,
                                                    @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.postEntry(id, userId));
    }

    @PutMapping("/{id}/lock")
    @Operation(summary = "Lock a posted journal entry (SAK compliance)")
    public ResponseEntity<JournalEntryEntity> lock(@PathVariable Long id,
                                                    @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.lockEntry(id, userId));
    }

    @PutMapping("/{id}/unlock")
    @Operation(summary = "Unlock a locked journal entry (admin override)")
    public ResponseEntity<JournalEntryEntity> unlock(@PathVariable Long id,
                                                      @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.unlockEntry(id, userId));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a posted/locked entry (creates reversal)")
    public ResponseEntity<JournalEntryEntity> cancel(@PathVariable Long id,
                                                      @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(service.cancelEntry(id, userId));
    }
}

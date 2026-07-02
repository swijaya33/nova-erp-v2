package com.novaerp.common.model;

/** Journal entry lifecycle status per SAK audit requirements */
public enum JournalEntryStatus {
    DRAFT,     // Not yet posted — can be edited freely
    POSTED,    // Posted to ledger — read-only
    LOCKED,    // Period closed — no modifications allowed (SAK compliance)
    CANCELLED  // Cancelled with reversal entry
}

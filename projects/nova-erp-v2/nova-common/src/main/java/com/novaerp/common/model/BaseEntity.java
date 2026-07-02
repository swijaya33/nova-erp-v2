package com.novaerp.common.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity for all Nova ERP v2 domain entities.
 * Provides: id, createdAt, updatedAt, deletedAt (soft delete), tenantId.
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    protected String tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Auto-inject tenant from TenantContext if not set
        if (this.tenantId == null || this.tenantId.isBlank()) {
            String currentTenant = com.novaerp.common.tenant.TenantContext.getCurrentTenant();
            if (currentTenant != null) {
                this.tenantId = currentTenant;
            } else {
                this.tenantId = "default";
            }
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }

    /** Soft delete marker */
    public boolean isDeleted() { return deletedAt != null; }
}

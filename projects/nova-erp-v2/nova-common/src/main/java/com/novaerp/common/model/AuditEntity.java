package com.novaerp.common.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@MappedSuperclass
public abstract class AuditEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    protected Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", length = 8, nullable = false)
    private OperationType operationType;

    @Column(name = "user_id")
    protected String userId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Lob
    @Column(name = "before_state")
    protected String beforeStateJson;

    @Lob
    @Column(name = "after_state", nullable = false)
    private Map<String, Object> afterStateMap;

    public enum OperationType { CREATE, UPDATE, DELETE }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public OperationType getOperationType() { return operationType; }
    public void setOperationType(OperationType operationType) { this.operationType = operationType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Map<String, Object> getAfterStateMap() { return afterStateMap; }
    public void setAfterStateMap(Map<String, Object> afterStateMap) { this.afterStateMap = afterStateMap; }

}

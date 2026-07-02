package com.novaerp.auth.dto;

import java.util.List;

/** User profile DTO — excludes sensitive fields like password */
public class UserProfile {

    private Long id;
    private String username;
    private String email;
    private String status;
    private String tenantId;
    private List<String> roles;

    public UserProfile() {}

    // --- Getters/Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

}

package com.novaerp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Registration request payload */
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 64)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255)
    private String password;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    /** Optional role names to assign on registration */
    private java.util.List<String> roles;

    public RegisterRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public java.util.List<String> getRoles() { return roles; }
    public void setRoles(java.util.List<String> roles) { this.roles = roles; }

}

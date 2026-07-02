package com.novaerp.auth.dto;

import java.util.List;

/** Login response payload */
public class LoginResponse {

    private String token;
    private String refreshToken;
    private long expiresIn;
    private List<String> roles;
    private UserProfile user;

    public LoginResponse() {}

    public LoginResponse(String token, String refreshToken, long expiresIn, List<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.roles = roles;
    }

    // --- Getters/Setters ---

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public UserProfile getUser() { return user; }
    public void setUser(UserProfile user) { this.user = user; }

}

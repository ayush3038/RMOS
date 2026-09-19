package com.rmos.dto.auth;

import com.rmos.domain.auth.Role;

public class LoginResponse {

    private String accessToken;
    private long expiresIn;
    private Role role;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, long expiresIn, Role role) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

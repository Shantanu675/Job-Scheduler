package com.scheduler.backend.auth.dto;

public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private String name;
    private String role;

    public AuthResponse(
            String token,
            Long userId,
            String email,
            String name,
            String role
    ) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
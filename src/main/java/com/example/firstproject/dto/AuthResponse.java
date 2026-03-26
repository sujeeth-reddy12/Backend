package com.example.firstproject.dto;

import com.example.firstproject.enums.Role;

public class AuthResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String message;
    private String token;

    public AuthResponse(Long id, String name, String email, Role role, String message, String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.message = message;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}

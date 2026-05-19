package com.careloop.dto;

import com.careloop.model.Role;

/** Login / register response with JWT token */
public class AuthResponse {

    private String token;
    private Long id;
    private String name;
    private String email;
    private Role role;
    private Integer reliabilityScore;
    private Boolean unreliable;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String name, String email, Role role,
                        Integer reliabilityScore, Boolean unreliable) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.reliabilityScore = reliabilityScore;
        this.unreliable = unreliable;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Integer getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(Integer reliabilityScore) { this.reliabilityScore = reliabilityScore; }

    public Boolean getUnreliable() { return unreliable; }
    public void setUnreliable(Boolean unreliable) { this.unreliable = unreliable; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

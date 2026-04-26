package com.foundit.dto.response;


public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String role;
    
    public AuthResponse() {}
    
    public AuthResponse(String token, Long id, String email, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return this.token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return this.type; }
    public void setType(String type) { this.type = type; }

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }
}

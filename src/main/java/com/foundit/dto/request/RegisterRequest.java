package com.foundit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String role; // Optional: STUDENT or ADMIN
    private String phone;
    private String department;

    public String getFullName() { return this.fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return this.role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return this.phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return this.department; }
    public void setDepartment(String department) { this.department = department; }
}

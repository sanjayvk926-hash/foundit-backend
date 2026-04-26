package com.foundit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('STUDENT', 'ADMIN') DEFAULT 'STUDENT'")
    private Role role;

    @Column(length = 15)
    private String phone;

    @Column(length = 100)
    private String department;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Role {
        STUDENT, ADMIN
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return this.fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return this.passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return this.role; }
    public void setRole(Role role) { this.role = role; }

    public String getPhone() { return this.phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return this.department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

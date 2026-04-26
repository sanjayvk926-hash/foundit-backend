package com.foundit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('MATCH_FOUND','ITEM_RESOLVED','ADMIN_UPDATE')")
    private NotificationType type;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRead = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        MATCH_FOUND, ITEM_RESOLVED, ADMIN_UPDATE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return this.user; }
    public void setUser(User user) { this.user = user; }

    public Match getMatch() { return this.match; }
    public void setMatch(Match match) { this.match = match; }

    public String getMessage() { return this.message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return this.type; }
    public void setType(NotificationType type) { this.type = type; }

    public Boolean getIsRead() { return this.isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.rmos.domain.auth;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "security_audit_events")
public class SecurityAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String username;

    private String details;

    @Column(nullable = false)
    private Instant timestamp;

    public SecurityAuditEvent() {
    }

    public SecurityAuditEvent(String eventType, String username, String details) {
        this.eventType = eventType;
        this.username = username;
        this.details = details;
        this.timestamp = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getUsername() {
        return username;
    }

    public String getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}

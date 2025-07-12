package com.kompozith.komflow.features.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Setter
@Getter
@Table(name = "aut_audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String objectType; // Class name

    @Column(nullable = false)
    private String objectId;

    private String connectedUserId;

    private String ipAddress;

    private String userAgent;

    private String userLocation;

    private String details;

    private Instant date;

    @PrePersist
    protected void onCreate() {
        this.date = Instant.now();
    }
}

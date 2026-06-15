package com.kompozith.komflow.features.auth.entity;

import com.kompozith.komflow.features.personnel.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "auth_password_reset_tokens", schema = "komflow")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** OTP à 6 chiffres envoyé par email. */
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    /** Token long (64 hex) émis après vérification OTP — utilisé pour l'étape « set new password ». */
    @Column(name = "reset_token", unique = true, length = 64)
    private String resetToken;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = Instant.now(); }

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
}

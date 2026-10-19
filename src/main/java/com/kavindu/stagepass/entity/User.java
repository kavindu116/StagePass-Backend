package com.kavindu.stagepass.entity;

import com.kavindu.stagepass.entity.Enums.AuthProvider;
import com.kavindu.stagepass.entity.Enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email",unique = true, nullable = false, length = 225)
    private String email;

    @Column(name = "password_hash",length = 100)
    private String passwordHash;

    @Column(name = "full_name",nullable = false,length = 120)
    private String fullName;

    @Column(name = "phone",length = 10)
    private String phone;

    @Column(name = "city",length = 60)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false,length = 20)
    private UserRole role = UserRole.CUSTOMER;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider",nullable = false,length = 20)
    private AuthProvider authProvider  = AuthProvider.LOCAL;

    @Column(name = "provider_id",length = 225)
    private String providerId;

    @Column(name = "email_verified",nullable = false)
    private boolean emailVerified = false;

    @Column(name = "notify_booking",nullable = false)
    private boolean notifyBooking = true;

    @Column(name = "notify_on_sale",nullable = false)
    private boolean notifyOnSale = true;

    @Column(name = "notify_reminder",nullable = false)
    private boolean notifyReminder = false;

    @Column(name = "enabled",nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at",nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt = Instant.now();


}

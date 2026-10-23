package com.kavindu.stagepass.entity;

import com.kavindu.stagepass.entity.Enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "payments")
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Orders order;

    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_payment_id", length = 100)
    private String providerPaymentId;

    @Column(name = "method", length = 20)
    private String method;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, columnDefinition = "CHAR(3)", length = 3)
    private String currency = "LKR";

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.INITIATED;

    @Column(name = "card_last4 ")
    private String cardLast4;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt = Instant.now();


}

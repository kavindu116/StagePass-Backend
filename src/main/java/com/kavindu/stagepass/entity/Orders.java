package com.kavindu.stagepass.entity;


import com.kavindu.stagepass.entity.Enums.OrderStatus;
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
@Table(name = "orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "public_ref",nullable = false,unique = true)
    private String publicRef;

    @JoinColumn(name = "user_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "show_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Shows show;

    @JoinColumn(name = "promo_code_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private PromoCodes promoCodes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false,length = 20)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "subtotal",precision = 12,scale = 2,nullable = false)
    private BigDecimal subtotal;

    @Column(name = "service_fee",precision = 12,scale = 2,nullable = false)
    private BigDecimal serviceFee = BigDecimal.ZERO;

    @Column(name = "discount",precision = 12,scale = 2,nullable = false)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total",precision = 12,scale = 2,nullable = false)
    private BigDecimal total;

    @Column(name = "currency",nullable = false,columnDefinition = "CHAR(3)",length = 3)
    private String currency = "LKR";

    @Column(name = "contact_name",nullable = false,length = 120)
    private String contactName;

    @Column(name = "contact_email",nullable = false,length = 225)
    private String contactEmail;

    @Column(name = "contact_phone",nullable = false,length = 10)
    private String contactPhone;

    @Column(name = "idempotency_key",nullable = false,unique = true)
    private String idempotencyKey;

    @Column(name = "hold_expires_at",nullable = false)
    private Instant holdExpiresAt;

    @Column(name = "confirmed_at",nullable = false)
    private Instant confirmedAt;

    @Column(name = "created_at",nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt = Instant.now();


}

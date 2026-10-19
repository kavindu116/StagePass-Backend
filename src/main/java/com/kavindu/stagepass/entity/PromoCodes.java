package com.kavindu.stagepass.entity;

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
@Table(name = "promo_codes")
public class PromoCodes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "event_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    @Column(name = "code",nullable = false,length = 30)
    private String code;

    @Column(name = "discount_type",nullable = false,length = 10)
    private String discountType;

    @Column(name = "discount_value",nullable = false,precision = 12,scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "used_count",nullable = false)
    private Integer usedCount = 0;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "active",nullable = false)
    private Boolean active = true;

    @Column(name = "created_at",nullable = false)
    private Instant createdAt = Instant.now();
}

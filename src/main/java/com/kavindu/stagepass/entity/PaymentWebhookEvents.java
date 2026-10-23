package com.kavindu.stagepass.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "payment_webhook_events")
public class PaymentWebhookEvents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider",nullable = false,length = 20)
    private String provider;

    @Column(name = "provider_event_id",nullable = false,length = 100)
    private String providerEventId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload",nullable = false)
    private Map<String, Object> payload;

    @Column(name = "received_at",nullable = false)
    private Instant receivedAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;



}

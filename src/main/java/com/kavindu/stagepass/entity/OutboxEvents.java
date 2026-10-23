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
@Table(name = "outbox_events")
public class OutboxEvents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type",nullable = false,length = 40)
    private String aggregateType;

    @Column(name = "aggregate_id",nullable = false,length = 40)
    private String aggregateId;

    @Column(name = "event_type",nullable = false,length = 60)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload",nullable = false)
    private Map<String, Object> payload;

    @Column(name = "created_at",nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "published_at")
    private Instant publishedAt;
}

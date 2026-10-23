package com.kavindu.stagepass.entity;

import com.kavindu.stagepass.entity.Enums.RefundRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "refund_requests")
public class RefundRequests {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Orders order;

    @JoinColumn(name = "requested_by",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User requestedBy;

    @Column(name = "reason",length = 500)
    private String reason;

    @Column(name = "status",nullable = false,length = 20)
    @Enumerated(EnumType.STRING)
    private RefundRequestStatus status;

    @JoinColumn(name = "reviewed_by",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "created_at",nullable = false)
    private Instant createdAt = Instant.now();

}

package com.kavindu.stagepass.entity;

import com.kavindu.stagepass.entity.Enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "tickets")
public class Tickets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code",nullable = false,unique = true)
    private UUID code = UUID.randomUUID();

    @JoinColumn(name = "order_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Orders order;

    @JoinColumn(name = "order_item_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private OrderItems orderItem;

    @JoinColumn(name = "show_seat_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ShowSeats showSeat;

    @Column(name = "status",nullable = false,length = 20)
    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.VALID;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @JoinColumn(name = "checked_in_by")
    @ManyToOne(fetch = FetchType.LAZY)
    private User checkedInBy;

    @Column(name = "created_at",nullable = false)
    private Instant createdAt = Instant.now();
}

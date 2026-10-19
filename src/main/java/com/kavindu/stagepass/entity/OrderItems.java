package com.kavindu.stagepass.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "order_items")
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Orders order;

    @JoinColumn(name = "show_seat_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ShowSeats showSeats;

    @JoinColumn(name = "tier_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Tier tier;

    @Column(name = "price",precision = 12,scale = 2,nullable = false)
    private BigDecimal price;

    @Column(name = "attendee_name",length = 120)
    private String attendeeName;


}

package com.kavindu.stagepass.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "tiers")
public class Tier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id",nullable = false,unique = true)
    private Event event;

    @Column(name = "name",nullable = false,length = 40)
    private String name;

    @Column(name = "price",nullable = false)
    private BigDecimal price;

    @Column(name = "currency",nullable = false)
    private char currency;

    @Column(name = "color",nullable = false)
    private char color;

    @Column(name = "sort_order",nullable = false)
    private Integer sortOrder;
}

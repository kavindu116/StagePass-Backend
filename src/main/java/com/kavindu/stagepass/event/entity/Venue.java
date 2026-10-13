package com.kavindu.stagepass.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 150)
    private String name;

    @Column(nullable = false,length = 150)
    private String address;

    @Column(nullable = false,length = 150)
    private String city;

    @Column(nullable = false,precision = 9,scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false,precision = 9,scale = 6)
    private BigDecimal longitude;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false,insertable = false,updatable = false)
    private Instant createdAt;

    @Column(nullable = false,insertable = false,updatable = false)
    private Instant updatedAt;

}

package com.kavindu.stagepass.entity;

import com.kavindu.stagepass.entity.Enums.ShowSeatStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.actuate.endpoint.Show;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "show_seats")
public class ShowSeats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name = "show_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Shows show;

    @JoinColumn(name = "venue_seat_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private VenueSeats venueSeats;

    @JoinColumn(name = "tier_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Tier tier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false,length = 20)
    private ShowSeatStatus status = ShowSeatStatus.AVAILABLE;

    @Column(name = "version",nullable = false)
    private Integer version = 0;

    @Column(name = "sold_at")
    private Instant soldAt;

    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt = Instant.now();
}

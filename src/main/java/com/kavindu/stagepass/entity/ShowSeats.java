package com.kavindu.stagepass.entity;

import com.kavindu.stagepass.entity.Enums.ShowSeatStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

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
    private Long id;                             // BIGINT -> Long

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Shows show;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_seat_id", nullable = false)
    private VenueSeats venueSeat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private Tier tier;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShowSeatStatus status = ShowSeatStatus.AVAILABLE;

    // Optimistic lock: Hibernate adds "AND version = ?" to every UPDATE
    // and throws OptimisticLockException if another transaction changed the row first.
    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "sold_at")
    private Instant soldAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

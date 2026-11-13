package com.kavindu.stagepass.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "venue_seats")
public class VenueSeats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    // Free text in the DB (MAIN, BALCONY, VIP-LEFT ...) — built by the admin layout builder
    @Builder.Default
    @Column(name = "section", nullable = false, length = 40)
    private String section = "MAIN";

    @Column(name = "row_label", nullable = false, length = 5)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private Short seatNumber;                    // SMALLINT -> Short

    @Column(name = "pos_x", nullable = false)
    private Short posX;

    @Column(name = "pos_y", nullable = false)
    private Short posY;

    @Builder.Default
    @Column(name = "accessible", nullable = false)
    private Boolean accessible = false;
}

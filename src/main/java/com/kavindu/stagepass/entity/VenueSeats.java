package com.kavindu.stagepass.entity;

import com.kavindu.stagepass.entity.Enums.Section;
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

    @JoinColumn(name = "venue_id",nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Venue venue;

    @Enumerated(EnumType.STRING)
    @Column(name = "section",nullable = false,length = 40)
    private Section section = Section.MAIN;

    @Column(name = "row_label",nullable = false,length = 5)
    private String rowLabel;

    @Column(name = "seat_number",nullable = false)
    private Integer seatNumber;

    @Column(name = "pos_x",nullable = false)
    private Integer posX;

    @Column(name = "pos_y",nullable = false)
    private Integer posY;

    @Column(name = "accessible",nullable = false)
    private Boolean accessible = false;



}

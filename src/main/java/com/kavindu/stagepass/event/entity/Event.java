package com.kavindu.stagepass.event.entity;

import com.kavindu.stagepass.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @OneToMany(fetch = FetchType.LAZY)
//    @JoinColumn(name = "organizer_id", nullable = false, unique = true)
    @Column(name = "organizer_id")
    private Long organizer_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false, unique = true)
    private Venue venue;

    @Column(name = "slug", nullable = false, length = 120)
    private String slug;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "short_label", length = 20)
    private String shortLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private EventCategory category;

    @Column(name = "description")
    private String description;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @Column(name = "tint_color", length = 7)
    private char tint_color;

    @Enumerated(EnumType.STRING)
    @Column(name = " age_restriction", nullable = false, length = 20)
    private AgeRestriction ageRestriction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_mode", nullable = false, length = 20)
    private SaleMode saleMode;

    @Column(name = "max_tickets_per_order", nullable = false, length = 20)
    private short maxTicketPerOrder;

    @Column(name = "sales_start_at")
    private Instant salesStartAt;

    @Column(name = "sales_end_at")
    private Instant salesEndAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}

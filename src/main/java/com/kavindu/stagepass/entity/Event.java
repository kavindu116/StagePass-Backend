package com.kavindu.stagepass.entity;

import com.kavindu.stagepass.entity.Enums.AgeRestriction;
import com.kavindu.stagepass.entity.Enums.EventCategory;
import com.kavindu.stagepass.entity.Enums.EventStatus;
import com.kavindu.stagepass.entity.Enums.SaleMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    // Plain id for now; becomes @ManyToOne User when auth is built
    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "short_label", length = 20)
    private String shortLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private EventCategory category;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "tint_color", length = 7)
    private String tintColor;                    // "#4B2E83"

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "age_restriction", nullable = false, length = 20)
    private AgeRestriction ageRestriction = AgeRestriction.ALL_AGES;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "sale_mode", nullable = false, length = 20)
    private SaleMode saleMode = SaleMode.DIRECT;

    @Builder.Default
    @Column(name = "max_tickets_per_order", nullable = false)
    private Short maxTicketsPerOrder = 6;

    @Column(name = "sales_start_at")
    private Instant salesStartAt;

    @Column(name = "sales_end_at")
    private Instant salesEndAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // ---- relationships (inverse side: Tier.event / Shows.event own the FK) ----

    @Builder.Default
    @OneToMany(mappedBy = "event")
    @OrderBy("sortOrder ASC")
    private List<Tier> tiers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "event")
    @OrderBy("startsAt ASC")
    private List<Shows> shows = new ArrayList<>();
}

package com.kavindu.stagepass.entity;

import com.kavindu.stagepass.entity.Enums.ShowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "shows")
public class Shows {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id",nullable = false)
    public Event event;

    @Column(name = "label",length = 60)
    private String label;

    @Column(name = "starts_at",nullable = false)
    private Instant startsAt;

    @Column(name = "doors_open_at")
    private Instant doorsOpenAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private ShowStatus status = ShowStatus.SCHEDULED;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt  = Instant.now();

}

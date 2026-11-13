package com.kavindu.stagepass.event.repository;

import com.kavindu.stagepass.entity.Enums.EventStatus;
import com.kavindu.stagepass.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    // Details page: event + venue + tiers in ONE query.
    // Shows are loaded separately (ShowRepository) -> avoids MultipleBagFetchException.
    @EntityGraph(attributePaths = {"venue", "tiers"})
    Optional<Event> findBySlugAndStatus(String slug, EventStatus status);

    // List page: only the venue (a to-one join is safe with pagination).
    @Override
    @EntityGraph(attributePaths = "venue")
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);
}

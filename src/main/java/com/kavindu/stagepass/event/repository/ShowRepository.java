package com.kavindu.stagepass.event.repository;

import com.kavindu.stagepass.entity.Shows;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ShowRepository extends JpaRepository<Shows, Long> {

    // Seat map: the show plus its event (to check it's published and read max tickets per order)
    @EntityGraph(attributePaths = "event")
    Optional<Shows> findWithEventById(Long id);

    // Details page: the event's shows in date order
    List<Shows> findByEventIdOrderByStartsAtAsc(Long eventId);

    // List cards: first show time per event, in ONE query
    @Query("""
            select new com.kavindu.stagepass.event.repository.EventFirstShow(s.event.id, min(s.startsAt))
            from Shows s
            where s.event.id in :eventIds
            group by s.event.id
            """)
    List<EventFirstShow> findFirstShowTimes(@Param("eventIds") Collection<Long> eventIds);
}

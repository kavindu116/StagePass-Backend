package com.kavindu.stagepass.event.repository;

import com.kavindu.stagepass.entity.Enums.ShowSeatStatus;
import com.kavindu.stagepass.entity.ShowSeats;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeats,Long> {
    /** List page: total + available seats per EVENT (all its shows), ONE query for the whole page. */
    @Query("""
            select new com.kavindu.stagepass.event.repository.SeatCount(
                       sh.event.id,
                       count(ss),
                       sum(case when ss.status = :available then 1 else 0 end))
            from ShowSeats ss
            join ss.show sh
            where sh.event.id in :eventIds
            group by sh.event.id
            """)
    List<SeatCount> countByEvent(@Param("eventIds") Collection<Long> eventIds,
                                 @Param("available") ShowSeatStatus available);

    /** Details page: total + available seats per SHOW of one event. */
    @Query("""
            select new com.kavindu.stagepass.event.repository.SeatCount(
                       ss.show.id,
                       count(ss),
                       sum(case when ss.status = :available then 1 else 0 end))
            from ShowSeats ss
            where ss.show.event.id = :eventId
            group by ss.show.id
            """)
    List<SeatCount> countByShow(@Param("eventId") Long eventId,
                                @Param("available") ShowSeatStatus available);
}


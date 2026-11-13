package com.kavindu.stagepass.event.repository;

import com.kavindu.stagepass.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TierRepository extends JpaRepository<Tier, Long> {

    @Query("""
            select new com.kavindu.stagepass.event.repository.EventMinPrice(t.event.id, min(t.price))
            from Tier t
            where t.event.id in :eventIds
            group by t.event.id
            """)
    List<EventMinPrice> findMinPrices(@Param("eventIds") Collection<Long> eventIds);
}

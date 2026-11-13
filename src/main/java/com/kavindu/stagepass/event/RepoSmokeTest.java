package com.kavindu.stagepass.event;

import com.kavindu.stagepass.entity.Enums.EventCategory;
import com.kavindu.stagepass.entity.Enums.EventStatus;
import com.kavindu.stagepass.entity.Event;
import com.kavindu.stagepass.event.dto.EventFilter;
import com.kavindu.stagepass.event.repository.EventRepository;
import com.kavindu.stagepass.event.repository.EventSpecifications;
import com.kavindu.stagepass.event.repository.ShowRepository;
import com.kavindu.stagepass.event.repository.TierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * TEMPORARY: checks the repositories against the dev seed data on startup.
 * Delete this class once the output matches the expected values.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class RepoSmokeTest implements CommandLineRunner {

    private final EventRepository events;
    private final TierRepository tiers;
    private final ShowRepository shows;

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) {
        Event e = events.findBySlugAndStatus("artist-live-colombo", EventStatus.PUBLISHED).orElseThrow();
        System.out.println(">> " + e.getTitle() + " @ " + e.getVenue().getCity() + ", tiers=" + e.getTiers().size()); // tiers=4
        System.out.println(">> shows=" + shows.findByEventIdOrderByStartsAtAsc(e.getId()).size());                  // 2

        count("CONCERT",      new EventFilter(EventCategory.CONCERT, null, null, null));          // 3
        count("city=negombo", new EventFilter(null, "negombo", null, null));                       // 1
        count("q=jazz",       new EventFilter(null, null, "jazz", null));                          // 1
        count("max=3000",     new EventFilter(null, null, null, new BigDecimal("3000")));          // 0

        Page<Event> page = events.findAll(
                EventSpecifications.withFilters(new EventFilter(null, null, null, null)),
                PageRequest.of(0, 20));
        List<Long> ids = page.getContent().stream().map(Event::getId).toList();
        System.out.println(">> min prices: " + tiers.findMinPrices(ids));     // 8 x 3500.00
        System.out.println(">> first shows: " + shows.findFirstShowTimes(ids)); // 8 rows
    }

    private void count(String label, EventFilter f) {
        System.out.println(">> " + label + " -> " + events.count(EventSpecifications.withFilters(f)));
    }
}

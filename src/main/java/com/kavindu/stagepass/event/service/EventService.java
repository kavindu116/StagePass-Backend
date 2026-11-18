package com.kavindu.stagepass.event.service;

import com.kavindu.stagepass.common.exception.ResourceNotFoundException;
import com.kavindu.stagepass.common.web.PageResponse;
import com.kavindu.stagepass.entity.Enums.EventStatus;
import com.kavindu.stagepass.entity.Enums.ShowSeatStatus;
import com.kavindu.stagepass.entity.Event;
import com.kavindu.stagepass.entity.Shows;
import com.kavindu.stagepass.event.dto.EventDetailResponse;
import com.kavindu.stagepass.event.dto.EventFilter;
import com.kavindu.stagepass.event.dto.EventSummeryResponse;
import com.kavindu.stagepass.event.repository.EventFirstShow;
import com.kavindu.stagepass.event.repository.EventMinPrice;
import com.kavindu.stagepass.event.repository.EventRepository;
import com.kavindu.stagepass.event.repository.EventSpecifications;
import com.kavindu.stagepass.event.repository.SeatCount;
import com.kavindu.stagepass.event.repository.ShowRepository;
import com.kavindu.stagepass.event.repository.ShowSeatRepository;
import com.kavindu.stagepass.event.repository.TierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final TierRepository tierRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    /**
     * List page. Always 4 queries for a whole page, no matter how many events (no N+1):
     * 1) events + venue (page)  2) min tier price  3) first show time  4) seat counts.
     * (+1 count query that Spring runs for the page total.)
     */
    public PageResponse<EventSummeryResponse> listEvents(EventFilter filter, Pageable pageable) {
        Page<Event> page = eventRepository.findAll(EventSpecifications.withFilters(filter), pageable);

        List<Long> ids = page.getContent().stream().map(Event::getId).toList();
        if (ids.isEmpty()) {
            // nothing to enrich — and avoids running "IN ()" queries
            return new PageResponse<>(List.of(), page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages());
        }

        Map<Long, BigDecimal> minPrices = tierRepository.findMinPrices(ids).stream()
                .collect(Collectors.toMap(EventMinPrice::eventId, EventMinPrice::minPrice));

        Map<Long, Instant> firstShows = showRepository.findFirstShowTimes(ids).stream()
                .collect(Collectors.toMap(EventFirstShow::eventId, EventFirstShow::firstStartsAt));

        Map<Long, SeatCount> seats = showSeatRepository.countByEvent(ids, ShowSeatStatus.AVAILABLE).stream()
                .collect(Collectors.toMap(SeatCount::id, Function.identity()));

        return PageResponse.from(page.map(e -> EventMapper.toSummary(
                e,
                minPrices.get(e.getId()),
                firstShows.get(e.getId()),
                seats.get(e.getId()))));
    }

    /**
     * Details page: event + venue + tiers (1 query), shows (1 query), seat counts per show (1 query).
     * Only PUBLISHED events are visible; a DRAFT slug gives 404, same as a slug that doesn't exist.
     */
    public EventDetailResponse getEvent(String slug) {
        Event event = eventRepository.findBySlugAndStatus(slug, EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Event", slug));

        List<Shows> shows = showRepository.findByEventIdOrderByStartsAtAsc(event.getId());

        Map<Long, SeatCount> seatsByShow = showSeatRepository.countByShow(event.getId(), ShowSeatStatus.AVAILABLE).stream()
                .collect(Collectors.toMap(SeatCount::id, Function.identity()));

        return EventMapper.toDetail(event, shows, seatsByShow);
    }
}

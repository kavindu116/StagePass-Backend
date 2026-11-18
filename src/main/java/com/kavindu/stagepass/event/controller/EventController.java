package com.kavindu.stagepass.event.controller;
import com.kavindu.stagepass.common.web.PageResponse;
import com.kavindu.stagepass.entity.Enums.EventCategory;
import com.kavindu.stagepass.event.dto.EventDetailResponse;
import com.kavindu.stagepass.event.dto.EventFilter;
import com.kavindu.stagepass.event.dto.EventSummeryResponse;
import com.kavindu.stagepass.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Public event catalogue")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "List published events",
            description = "Filters are optional and combined with AND. category is case-insensitive (concert / CONCERT).")
    public PageResponse<EventSummeryResponse> listEvents(
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal maxPrice,
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return eventService.listEvents(new EventFilter(category, city, q, maxPrice), pageable);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get one published event with its venue, shows and price tiers")
    public EventDetailResponse getEvent(@PathVariable String slug) {
        return eventService.getEvent(slug);
    }
}

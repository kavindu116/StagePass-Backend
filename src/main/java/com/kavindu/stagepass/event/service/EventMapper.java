package com.kavindu.stagepass.event.service;

import com.kavindu.stagepass.entity.Event;
import com.kavindu.stagepass.entity.Shows;
import com.kavindu.stagepass.entity.Tier;
import com.kavindu.stagepass.entity.Venue;
import com.kavindu.stagepass.event.dto.Availability;
import com.kavindu.stagepass.event.dto.EventDetailResponse;
import com.kavindu.stagepass.event.dto.EventDetailResponse.ShowResponse;
import com.kavindu.stagepass.event.dto.EventDetailResponse.TierResponse;
import com.kavindu.stagepass.event.dto.EventDetailResponse.VenueResponse;
import com.kavindu.stagepass.event.dto.EventSummeryResponse;
import com.kavindu.stagepass.event.repository.SeatCount;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class EventMapper {

    private EventMapper() {

    }

    static EventSummeryResponse toSummary(Event e, BigDecimal priceFrom, Instant firstShowAt, SeatCount seats) {
        Venue v = e.getVenue();
        return new EventSummeryResponse(
                e.getId(),
                e.getSlug(),
                e.getTitle(),
                e.getShortLabel(),
                e.getCategory(),
                e.getTintColor(),
                e.getPosterUrl(),
                v.getName(),
                v.getCity(),
                firstShowAt,
                priceFrom,
                e.getSaleMode(),
                availability(seats));
    }

    static EventDetailResponse toDetail(Event e, List<Shows> shows, Map<Long, SeatCount> seatsByShow) {
        List<TierResponse> tiers = e.getTiers().stream()
                .map(EventMapper::toTier)
                .toList();

        BigDecimal priceFrom = e.getTiers().stream()
                .map(Tier::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(null);

        List<ShowResponse> showResponses = shows.stream()
                .map(s -> toShow(s, seatsByShow.get(s.getId())))
                .toList();

        return new EventDetailResponse(
                e.getId(),
                e.getSlug(),
                e.getTitle(),
                e.getShortLabel(),
                e.getCategory(),
                e.getDescription(),
                e.getPosterUrl(),
                e.getTintColor(),
                e.getAgeRestriction(),
                e.getSaleMode(),
                e.getMaxTicketsPerOrder(),
                priceFrom,
                toVenue(e.getVenue()),
                showResponses,
                tiers);
    }

    private static VenueResponse toVenue(Venue v) {
        return new VenueResponse(v.getId(), v.getName(), v.getAddress(), v.getCity(), v.getLatitude(), v.getLongitude());
    }

    private static TierResponse toTier(Tier t) {
        return new TierResponse(t.getId(), t.getName(), t.getPrice(), t.getCurrency(), t.getColor());
    }

    private static ShowResponse toShow(Shows s, SeatCount seats) {
        long available = seats == null ? 0 : seats.available();
        return new ShowResponse(
                s.getId(),
                s.getLabel(),
                s.getStartsAt(),
                s.getDoorsOpenAt(),
                s.getStatus(),
                available,
                availability(seats));
    }

    private static Availability availability(SeatCount seats) {
        return seats == null ? Availability.SOLD_OUT : Availability.of(seats.total(), seats.available());
    }
}

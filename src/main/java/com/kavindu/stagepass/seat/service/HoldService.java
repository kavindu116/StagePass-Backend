package com.kavindu.stagepass.seat.service;

import com.kavindu.stagepass.common.exception.BusinessRuleException;
import com.kavindu.stagepass.common.exception.ResourceNotFoundException;
import com.kavindu.stagepass.common.exception.SeatUnavailableException;
import com.kavindu.stagepass.entity.Enums.EventStatus;
import com.kavindu.stagepass.entity.Enums.ShowSeatStatus;
import com.kavindu.stagepass.entity.Enums.ShowStatus;
import com.kavindu.stagepass.entity.Event;
import com.kavindu.stagepass.entity.ShowSeats;
import com.kavindu.stagepass.entity.Shows;
import com.kavindu.stagepass.event.repository.ShowSeatRepository;
import com.kavindu.stagepass.seat.dto.HoldRequest;
import com.kavindu.stagepass.seat.dto.HoldResponse;
import com.kavindu.stagepass.seat.dto.HoldResponse.HeldSeat;
import com.kavindu.stagepass.seat.hold.HoldProperties;
import com.kavindu.stagepass.seat.hold.SeatHoldStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HoldService {

    private static final Comparator<ShowSeats> SEAT_ORDER = Comparator
            .comparing((ShowSeats ss) -> ss.getVenueSeat().getPosY())
            .thenComparing(ss -> ss.getVenueSeat().getPosX());

    private final ShowSeatRepository showSeatRepository;
    private final SeatHoldStore holdStore;
    private final HoldProperties holdProperties;

    public HoldResponse hold(HoldRequest request) {
        List<Long> seatIds = request.seatIds().stream().distinct().sorted().toList();
        if (seatIds.size() != request.seatIds().size()) {
            throw new BusinessRuleException("The same seat was requested twice");
        }

        // 1) Postgres: load and validate the seats (one query)
        List<ShowSeats> seats = showSeatRepository.findWithDetailsByIdIn(seatIds);
        boolean allOnThisShow = seats.size() == seatIds.size()
                && seats.stream().allMatch(ss -> ss.getShow().getId().equals(request.showId()));
        if (!allOnThisShow) {
            throw new BusinessRuleException("Some seats do not exist or belong to another show");
        }

        Shows show = seats.get(0).getShow();
        Event event = show.getEvent();
        ensureOnSale(event, show);

        if (seatIds.size() > event.getMaxTicketsPerOrder()) {
            throw new BusinessRuleException("You can book at most " + event.getMaxTicketsPerOrder() + " seats per order");
        }

        List<String> notSellable = seats.stream()
                .filter(ss -> ss.getStatus() != ShowSeatStatus.AVAILABLE)
                .sorted(SEAT_ORDER)
                .map(HoldService::label)
                .toList();
        if (!notSellable.isEmpty()) {
            throw new SeatUnavailableException(notSellable);
        }

        // 2) Redis: hold all seats or none (Lua script = atomic)
        String holdId = UUID.randomUUID().toString();
        var conflict = holdStore.tryHold(show.getId(), seatIds, holdId, holdProperties.ttl());
        if (conflict.isPresent()) {
            Map<Long, ShowSeats> byId = seats.stream().collect(Collectors.toMap(ShowSeats::getId, Function.identity()));
            throw new SeatUnavailableException(List.of(label(byId.get(conflict.get()))));
        }

        log.info("Hold {} created: show={} seats={}", holdId, show.getId(), seatIds);
        return toResponse(holdId, show, event, seats, Instant.now().plus(holdProperties.ttl()));
    }

    public HoldResponse getHold(String holdId) {
        SeatHoldStore.Hold hold = holdStore.find(holdId)
                .orElseThrow(() -> new ResourceNotFoundException("Hold", holdId));

        List<ShowSeats> seats = showSeatRepository.findWithDetailsByIdIn(hold.seatIds());
        if (seats.isEmpty()) {
            throw new ResourceNotFoundException("Hold", holdId);
        }
        Shows show = seats.get(0).getShow();
        return toResponse(holdId, show, show.getEvent(), seats, Instant.now().plus(hold.remaining()));
    }

    /** Idempotent: releasing an expired or unknown hold is not an error. */
    public void release(String holdId) {
        holdStore.find(holdId).ifPresent(hold -> {
            long released = holdStore.release(hold);
            log.info("Hold {} released ({} seats)", holdId, released);
        });
    }

    private static void ensureOnSale(Event event, Shows show) {
        Instant now = Instant.now();
        if (event.getStatus() != EventStatus.PUBLISHED || show.getStatus() != ShowStatus.SCHEDULED) {
            throw new BusinessRuleException("This show is not on sale");
        }
        if (event.getSalesStartAt() != null && now.isBefore(event.getSalesStartAt())) {
            throw new BusinessRuleException("Ticket sales have not started yet");
        }
        if (event.getSalesEndAt() != null && now.isAfter(event.getSalesEndAt())) {
            throw new BusinessRuleException("Ticket sales have ended");
        }
    }

    private static HoldResponse toResponse(String holdId, Shows show, Event event, List<ShowSeats> seats, Instant expiresAt) {
        List<HeldSeat> held = seats.stream()
                .sorted(SEAT_ORDER)
                .map(ss -> new HeldSeat(ss.getId(), label(ss), ss.getTier().getName(), ss.getTier().getPrice(), ss.getTier().getColor()))
                .toList();
        BigDecimal subtotal = held.stream().map(HeldSeat::price).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new HoldResponse(holdId, show.getId(), event.getSlug(), expiresAt, held, subtotal);
    }

    private static String label(ShowSeats ss) {
        return ss.getVenueSeat().getRowLabel() + ss.getVenueSeat().getSeatNumber();
    }
}

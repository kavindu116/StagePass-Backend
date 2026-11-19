package com.kavindu.stagepass.seat.service;


import com.kavindu.stagepass.common.exception.ResourceNotFoundException;
import com.kavindu.stagepass.entity.Enums.EventStatus;
import com.kavindu.stagepass.entity.ShowSeats;
import com.kavindu.stagepass.entity.Shows;
import com.kavindu.stagepass.entity.VenueSeats;
import com.kavindu.stagepass.event.repository.ShowRepository;
import com.kavindu.stagepass.event.repository.ShowSeatRepository;
import com.kavindu.stagepass.seat.dto.SeatMapResponse;
import com.kavindu.stagepass.seat.hold.SeatHoldStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatMapService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final SeatHoldStore holdStore;

    public SeatMapResponse getSeatMap(Long showId) {
        Shows show = showRepository.findWithEventById(showId)
                .filter(s -> s.getEvent().getStatus() == EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Show", showId));

        List<ShowSeats> seats = showSeatRepository.findSeatMap(showId);
        Set<Long> held = holdStore.heldAmong(seats.stream().map(ShowSeats::getId).toList());

        Map<String, List<SeatMapResponse.SeatResponse>> rows = new LinkedHashMap<>();

        for (ShowSeats seat : seats) {
            VenueSeats vs = seat.getVenueSeat();
            rows.computeIfAbsent(vs.getRowLabel(), r -> new ArrayList<>()).add(new SeatMapResponse.SeatResponse(
                    seat.getId(),
                    vs.getRowLabel() + vs.getSeatNumber(),
                    vs.getRowLabel(),
                    vs.getSeatNumber(),
                    vs.getPosX(),
                    seat.getTier().getName(),
                    state(seat, held))

            );
        }

        return new SeatMapResponse(
                show.getId(),
                show.getEvent().getSlug(),
                show.getEvent().getMaxTicketsPerOrder(),
                rows.entrySet().stream().map(e -> new SeatMapResponse.RowResponse(e.getKey(), e.getValue())).toList());
    }


    private static SeatMapResponse.SeatState state(ShowSeats ss, Set<Long> held) {
        return switch (ss.getStatus()) {
            case SOLD -> SeatMapResponse.SeatState.SOLD;                 // sold always wins over a stale hold
            case BLOCKED -> SeatMapResponse.SeatState.BLOCKED;
            case AVAILABLE ->
                    held.contains(ss.getId()) ? SeatMapResponse.SeatState.HELD : SeatMapResponse.SeatState.AVAILABLE;
        };
    }
}

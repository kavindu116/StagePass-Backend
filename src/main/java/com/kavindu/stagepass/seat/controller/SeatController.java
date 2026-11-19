package com.kavindu.stagepass.seat.controller;

import com.kavindu.stagepass.seat.dto.HoldRequest;
import com.kavindu.stagepass.seat.dto.HoldResponse;
import com.kavindu.stagepass.seat.dto.SeatMapResponse;
import com.kavindu.stagepass.seat.service.HoldService;
import com.kavindu.stagepass.seat.service.SeatMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Seats & holds", description = "Live seat map and 10-minute seat holds")
public class SeatController {
    private final SeatMapService seatMapService;
    private final HoldService holdService;

    @GetMapping("/shows/{showId}/seats")
    @Operation(summary = "Seat map of a show: AVAILABLE / HELD / SOLD / BLOCKED per seat")
    public SeatMapResponse seatMap(@PathVariable Long showId) {
        return seatMapService.getSeatMap(showId);
    }

    @PostMapping("/holds")
    @Operation(summary = "Hold seats for 10 minutes (all or nothing). 409 if any seat was taken first.")
    public ResponseEntity<HoldResponse> hold(@Valid @RequestBody HoldRequest request) {
        HoldResponse hold = holdService.hold(request);
        return ResponseEntity.created(URI.create("/api/holds/" + hold.holdId())).body(hold);
    }

    @GetMapping("/holds/{holdId}")
    @Operation(summary = "Get a hold (seats, prices, expiry). 404 once it has expired.")
    public HoldResponse getHold(@PathVariable UUID holdId) {
        return holdService.getHold(holdId.toString());
    }

    @DeleteMapping("/holds/{holdId}")
    @Operation(summary = "Release a hold early (e.g. user goes back to change seats)")
    public ResponseEntity<Void> release(@PathVariable UUID holdId) {
        holdService.release(holdId.toString());
        return ResponseEntity.noContent().build();
    }
}

package com.kavindu.stagepass.seat.dto;

import java.util.List;

public record SeatMapResponse(
        Long showId,
        String eventSlug,
        int maxSeatsPerOrder,
        List<RowResponse> rows
) {
    public record RowResponse(String label, List<SeatResponse> seats) {
    }


    public record SeatResponse(
            Long id,            // show_seats.id — send this back in POST /api/holds
            String label,       // "C7"
            String row,
            int number,
            int posX,           // a jump of more than 1 between neighbours = aisle
            String tier,        // tier name: VIP, Gold ...
            SeatState status
    ) {
    }

    public enum SeatState {
        AVAILABLE,
        HELD,       // someone has it in checkout (Redis)
        SOLD,
        BLOCKED
    }

}

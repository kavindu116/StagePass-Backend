package com.kavindu.stagepass.seat.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record HoldResponse (
        String holdId,
        Long showId,
        String eventSlug,
        Instant expiresAt,
        List<HeldSeat> seats,
        BigDecimal subtotal
) {


    public record HeldSeat(
            Long id,
            String label,
            String tier,
            BigDecimal price,
            String color
    ) {
    }

}

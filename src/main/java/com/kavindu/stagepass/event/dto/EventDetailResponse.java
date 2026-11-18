package com.kavindu.stagepass.event.dto;

import com.kavindu.stagepass.entity.Enums.AgeRestriction;
import com.kavindu.stagepass.entity.Enums.EventCategory;
import com.kavindu.stagepass.entity.Enums.SaleMode;
import com.kavindu.stagepass.entity.Enums.ShowStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record EventDetailResponse (
        Long id,
        String slug,
        String title,
        String shortLabel,
        EventCategory category,
        String description,
        String posterUrl,
        String tintColor,
        AgeRestriction ageRestriction,
        SaleMode saleMode,
        int maxTicketsPerOrder,
        BigDecimal priceFrom,
        VenueResponse venue,
        List<ShowResponse> shows,
        List<TierResponse> tiers
) {
    public record VenueResponse(
            Long id,
            String name,
            String address,
            String city,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
    }

    public record ShowResponse(
            Long id,
            String label,
            Instant startsAt,
            Instant doorsOpenAt,
            ShowStatus status,
            long availableSeats,
            Availability availability
    ) {
    }

    public record TierResponse(
            Long id,
            String name,
            BigDecimal price,
            String currency,
            String color
    ) {
    }
}

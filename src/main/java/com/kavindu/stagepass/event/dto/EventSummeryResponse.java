package com.kavindu.stagepass.event.dto;

import com.kavindu.stagepass.entity.Enums.EventCategory;
import com.kavindu.stagepass.entity.Enums.SaleMode;

import java.math.BigDecimal;
import java.time.Instant;

public record EventSummeryResponse(
        Long id,
        String slug,
        String title,
        String shortLabel,
        EventCategory category,
        String tintColor,
        String posterUrl,
        String venueName,
        String city,
        Instant firstShowAt,
        BigDecimal priceFrom,
        SaleMode saleMode,
        Availability availability
)  {
}

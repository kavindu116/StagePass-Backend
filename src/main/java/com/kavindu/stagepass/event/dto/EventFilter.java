package com.kavindu.stagepass.event.dto;

import com.kavindu.stagepass.entity.Enums.EventCategory;

import java.math.BigDecimal;

public record EventFilter(
        EventCategory category,
        String city,
        String q,
        BigDecimal maxPrice
) {
}

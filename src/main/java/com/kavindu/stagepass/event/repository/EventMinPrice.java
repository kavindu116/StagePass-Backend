package com.kavindu.stagepass.event.repository;

import java.math.BigDecimal;

/** Cheapest tier price per event — used for "From LKR 3,500" on list cards. */
public record EventMinPrice(Long eventId, BigDecimal minPrice) {
}

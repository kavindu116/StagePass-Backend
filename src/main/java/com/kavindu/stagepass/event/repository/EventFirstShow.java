package com.kavindu.stagepass.event.repository;

import java.time.Instant;

/** Earliest show time per event — used for the date on list cards. */
public record EventFirstShow(Long eventId, Instant firstStartsAt) {
}

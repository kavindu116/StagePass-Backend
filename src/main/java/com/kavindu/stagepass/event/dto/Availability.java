package com.kavindu.stagepass.event.dto;

public enum Availability {
    AVAILABLE,
    SELLING_FAST,
    SOLD_OUT;

    public static Availability of(long totalSeats,long availableSeats) {
        if (totalSeats == 0 || availableSeats == 0) {
            return SOLD_OUT;
        }
        if (availableSeats * 5 <= totalSeats) {
            return SELLING_FAST;
        }
        return AVAILABLE;
    }

}

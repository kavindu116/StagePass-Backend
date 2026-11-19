package com.kavindu.stagepass.common.exception;

import java.util.List;

public class SeatUnavailableException extends RuntimeException {

    private final List<String> seats;

    public SeatUnavailableException(List<String> seats) {
        super("Seat " +String.join(", ", seats) + " is not available");
        this.seats = List.copyOf(seats);
    }

    private List<String> getSeats() {
        return seats;
    }

}

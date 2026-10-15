package com.capgemini.freebirds.model;

import java.time.LocalDateTime;

public record BoatReservation(int uniqueBoatReservationId, Boat boat, LocalDateTime beginDateTime, LocalDateTime endDateTime, double price) {
}
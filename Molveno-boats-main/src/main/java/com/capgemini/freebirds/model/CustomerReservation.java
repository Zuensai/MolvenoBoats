package com.capgemini.freebirds.model;

import java.time.LocalDateTime;

public record CustomerReservation(int uniqueReservationId, BoatReservation[] boat, Customer customer, LocalDateTime beginDateTime, LocalDateTime endDateTime, double totalPrice) {}

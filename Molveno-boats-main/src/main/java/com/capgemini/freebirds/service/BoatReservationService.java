package com.capgemini.freebirds.service;


import com.capgemini.freebirds.data.BoatReservationRepo;
import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.model.BoatReservation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


public class BoatReservationService {
    BoatReservationRepo boatReservationRepo = new BoatReservationRepo();
    List<BoatReservation> reservations = boatReservationRepo.load();

    public static BoatReservation create(Boat boat, LocalDateTime beginDateTime, LocalDateTime endDateTime) {

        double duration = Duration.between(beginDateTime, endDateTime).toMinutes();
        double price;

        if (duration % 30 == 0) {
            price = boat.pricePer30Min() * (duration / 30);
        } else {
            price = boat.pricePer30Min() * ((duration - 15) / 30) + boat.pricePer15Min();
        }

        return new BoatReservation(UniqueIdCreator.getNextUniqueId(), boat, beginDateTime, endDateTime, price);
    }

    public BoatReservation read(int id) {
        if (reservations == null || reservations.isEmpty()) {
            System.out.println("Database is empty!");
            return null;
        }

        for (BoatReservation reservation : reservations) {
            if (reservation.uniqueBoatReservationId() == id) {
                return reservation;
            }
        }
        return null;
    }
}

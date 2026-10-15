package com.capgemini.freebirds.service;

import com.capgemini.freebirds.data.ReservationRepo;
import com.capgemini.freebirds.data.ReservationRepoInterface;
import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.model.BoatReservation;
import com.capgemini.freebirds.model.Customer;
import com.capgemini.freebirds.model.CustomerReservation;
import com.capgemini.freebirds.model.CancellationPolicy;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;
import java.time.temporal.ChronoUnit;

public class ReservationService implements ReservationServiceInterface {

    ReservationRepoInterface reservationRepo = new ReservationRepo();
    List<CustomerReservation> reservations = reservationRepo.load();

    public CustomerReservation create(Customer customer, List<Boat> boats, LocalDateTime beginDateTime, LocalDateTime endDateTime) {

        for (Boat boat : boats) {
            if (!isBoatAvailable(boat, beginDateTime, endDateTime)) {
                throw new IllegalStateException("Boat " + boat.uniqueId() + " is already booked on this time slot");
            }
        }

        List<BoatReservation> boatReservationsList = new ArrayList<>();

        for (Boat boat : boats) {
            BoatReservation br = BoatReservationService.create(boat, beginDateTime, endDateTime);
            boatReservationsList.add(br);
        }

        BoatReservation[] boatReservations = boatReservationsList.toArray(new BoatReservation[0]);
        double totalPrice = 0;
        for (int i = 0; i < boatReservations.length; i++) {
            totalPrice = totalPrice + boatReservations[i].price();
        }

        int uniqueId = UniqueIdCreator.getNextUniqueId();
        CustomerReservation reservation = new CustomerReservation(uniqueId, boatReservations, customer, beginDateTime, endDateTime, totalPrice);

        reservations.add(reservation);
        reservationRepo.save(reservations);

        return reservation;
    }

    @Override
    public CustomerReservation create(Customer customer, BoatReservation[] boats, LocalDateTime beginDateTime, LocalDateTime endDateTime) {
        return null;
    }

    public CustomerReservation read(int id) {

        for (CustomerReservation reservation : reservations) {

            if (reservation == null) {
                System.out.println("Database is empty!");
                return null;
            }
            if (reservation.uniqueReservationId() == id) {
                return reservation;
            }
        }
        return null;
    }

    public CustomerReservation read(String lastName) {

        for (CustomerReservation reservation : reservations) {

            if (Objects.equals(reservation.customer().lastName(), lastName)) {
                return reservation;
            }
        }

        return null;
    }

    public CustomerReservation update(CustomerReservation reservation) {
        List<CustomerReservation> toRemove = new ArrayList<>();
        for (CustomerReservation reservationInList : reservations) {
            if (reservationInList == null) {
                break;
            } else if (reservationInList.uniqueReservationId() == reservation.uniqueReservationId()) {
                toRemove.add(reservationInList);
            }
        }
        reservations.removeAll(toRemove);
        reservations.add(reservation);
        while (reservations.contains(null)) {
            reservations.remove(null);
        }
        reservationRepo.save(reservations);
        return reservation;
    }
    public void delete(CustomerReservation reservation) {
        reservations.removeIf(r -> r.uniqueReservationId() == reservation.uniqueReservationId());
        reservationRepo.save(reservations);
    }

    public List<CustomerReservation> getAll() {
        return reservations;
    }

    public boolean isBoatAvailable(Boat boat, LocalDateTime begin, LocalDateTime end) {
        for (CustomerReservation existing : reservations) {
            for (BoatReservation br : existing.boat()) {
                boolean sameBoat = br.boat().uniqueId() == boat.uniqueId();
                boolean overlaps = begin.isBefore(br.endDateTime()) && end.isAfter(br.beginDateTime());
                if (sameBoat && overlaps) {
                    return false;
                }
            }
        }
        return true;
    }
    

    public CancellationPolicy getCancellationPolicy(CustomerReservation reservation) {
        long hours = java.time.temporal.ChronoUnit.HOURS.between(
                LocalDateTime.now(), reservation.beginDateTime()
        );
        return CancellationPolicy.hoursUntilStart(hours);
    }

    public double calculateRefund(CustomerReservation reservation) {
        double total = java.util.Arrays.stream(reservation.boat())
                .mapToDouble(BoatReservation::price)
                .sum();
        int feePercentage = getCancellationPolicy(reservation).getFeePercent();
        return total * (1.0 - feePercentage / 100.0);
    }

    public void cancel(CustomerReservation reservation) {
        delete(reservation);
        reservationRepo.save(reservations);
    }

    public static double totalCost(ArrayList<Boat> boats, LocalDateTime begin, LocalDateTime end) {

        double duration = Duration.between(begin, end).toMinutes();
        double cost = 0;
        double price;

        for(Boat b : boats) {
            if (duration % 30 == 0) {
                price = b.pricePer30Min() * (duration / 30);
            } else {
                price = b.pricePer30Min() * ((duration - 15) / 30) + b.pricePer15Min();
            }
            cost = cost + price;
        }
        if (cost < 0) cost = 0;
        return cost;
    }
}
package com.capgemini.freebirds.service;

import com.capgemini.freebirds.data.BoatRepo;
import com.capgemini.freebirds.data.BoatRepoInterface;
import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.data.BoatExcelRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BoatService implements BoatServiceInterface {

    static BoatRepoInterface BoatRepo = new BoatRepo();
    static List<Boat> BoatList = BoatRepo.load();

    public static Boat create(String type, int capacity, String[] properties, double pricePer15Min, double pricePer30Min) {
        int id = UniqueIdCreator.getNextUniqueId();
        // moet hier gaan checken op welke prijs er bij moet komen.
        Boat newBoat = new Boat(id, type, capacity, properties, pricePer15Min, pricePer30Min);
        if (Objects.equals(read(id), newBoat)) {
            System.out.println("Boat already exists!");
            return null;
        }
        BoatList.add(newBoat);
        BoatRepo.save(BoatList);

        return newBoat;
    }

    public static Boat read(int id) {

        for (Boat boat : BoatList) {

            if (boat == null) {
                System.out.println("Database is empty!");
                return null;
            }
            if (boat.uniqueId() == id) {
                return boat;
            }
        }
        return null;
    }

    public static double calculatePricePerTimeBox(Boat boat) {

        double pricePer15 = boat.pricePer15Min();
        double pricePer30 = boat.pricePer30Min();

        throw new RuntimeException("Invalid Boat Reservation information");
    }

    public List<Boat> getBoatList() {
        return BoatList;
    }

    public List<Boat> getAvailableBoats(LocalDateTime begin, LocalDateTime end) {
        ReservationService reservationService = new ReservationService();
        List<Boat> available = new ArrayList<>();
        for (Boat boat : BoatList) {
            if (reservationService.isBoatAvailable(boat, begin, end)) {
                available.add(boat);
            }
        }
        return available;
    }

    public Boat read(String type) {

        for (Boat boat : BoatList) {

            if (Objects.equals(boat.type(), type)) {
                return boat;
            }
        }
        return null;
    }

    public boolean ifExists(Boat boatToCheck) {

        for (Boat boatInList : BoatList) {

            if (boatInList == boatToCheck) {
                return true;
            }
        }
        return false;
    }

    public void update(Boat boat) {

        List<Boat> toRemove = new ArrayList<>();

        for (Boat boats : BoatList) {
            if (boat == null) {
                break;
            } else if (boat.uniqueId() == boats.uniqueId()) {
                toRemove.add(boats);
            }
        }

        BoatList.removeAll(toRemove);
        BoatList.add(boat);

        while (BoatList.contains(null)) {
            BoatList.remove(null);
        }
        BoatRepo.save(BoatList);
    }

    public void delete(Boat boatToDelete) {
        BoatList.remove(boatToDelete);
        BoatRepo.save(BoatList);
    }
}
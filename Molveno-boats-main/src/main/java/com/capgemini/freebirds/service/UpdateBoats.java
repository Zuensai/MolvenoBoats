package com.capgemini.freebirds.service;

import com.capgemini.freebirds.data.BoatExcelRepo;
import com.capgemini.freebirds.data.BoatRepo;
import com.capgemini.freebirds.data.BoatReservationRepo;
import com.capgemini.freebirds.data.ReservationRepo;
import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.model.BoatReservation;
import com.capgemini.freebirds.model.CustomerReservation;

import java.time.LocalDateTime;
import java.util.*;

public class UpdateBoats {
    public static void update() {
        // de excel data ophalen
        List<Boat> excelBoats = GetAllExcelBoats();

        // de boten van de Json ophalen
        List<Boat> jsonBoats = getExistingBoatsFromJson();

        //exta lijsten ophalen
        List<Boat> duplicates = GetDuplicatesJsonExcel(jsonBoats, excelBoats);
        List<Boat> newEntries = getNewEntries(jsonBoats, excelBoats, duplicates);
        List<Boat> boatsToDelete = getBoatsToDelete(jsonBoats, excelBoats, duplicates);

        // aanmaken van de uiteindelijke Boatlijst
        List<Boat> boatsToPutInJson = new ArrayList<>();

        //inhoud toevoegen aan de uiteindelijke lijst
        boatsToPutInJson.addAll(duplicates);
        boatsToPutInJson.addAll(newEntries);
        DeleteBoatsFromList(boatsToDelete, boatsToPutInJson);


        //tonen of het goed gaat
//        System.out.println();
//        PrintBoatList(excelBoats, "excelBoats");
//        PrintBoatList(jsonBoats, "Jsonboats");
//        PrintBoatList(newEntries, "newEntries");
//        PrintBoatList(duplicates, "Duplicates");
//        PrintBoatList(boatsToDelete, "boatsToDelete");


        // de lijst van boten in de Json zetten
        SaveToJson(boatsToPutInJson);


        //oude commenting
        // de oude Json ophalen en zetten in een ArrayList van Boten
        // maken van een nieuwe lege Json
        // een lijst maken met alle verschillende soort boten uit de excel (zonder hoeveelheden)
        // bij in deze lijst gaan kijken hoeveel er zijn toegestaan om toegevoegd te worden aan de nieuwe json lijst
        // for loop om per boot type het volgende te doen:
        // indien er meer boten door mogen dan dat er in de json zitten,
        // variable voor te kijken wat het bijpassende getal is vanuit de excel
        // dan alle oude boten overnemen en de nieuwe boten aanmaken
        // indien er te veel boten zijn in de Json eerst alle boten ophalen die al een bootreservering hebben
        // zoek naar alle bootreserveringen
        // indien er minder gereserveerde boten zijn dan er door mogen
        // de gereserveerde boten doorlaten
        // zoeken naar de boten die geen reservering erop hebben die wel het juiste type en capacity hebben
        // de boten met het laagste id doorlaten om zo tot de hoeveelheid te komen van de nieuwe boatAmount
        // anders foutmelding met daarin welke boten er allemaal een bootreservering hebben op hun naam
        // de nieuwe Json over de oude Json plakken
        // filter hieruit alleen de bootreserveringen van hetzelfde type en capaciteit


    }

    private static void DeleteBoatsFromList(List<Boat> boatsToDelete, List<Boat> boatsToPutInJson) {
        List<Integer> idsToDelete = new ArrayList<>();
        for (Boat boat : boatsToDelete) {
            idsToDelete.add(boat.uniqueId());
        }
        boatsToPutInJson.removeIf(boat -> idsToDelete.contains(boat.uniqueId()));
    }

    private static List<Boat> getBoatsToDelete(List<Boat> jsonBoats, List<Boat> excelBoats, List<Boat> duplicates) {
        List<Boat> boatsToDelete = new ArrayList<>();

        var jsonNumberOfBoatsPerType = GetAmountPerUniqueBoat(jsonBoats, "jsonBoats");
        var excelNumberOfBoatsPerType = GetAmountPerUniqueBoat(excelBoats, "excelBoats");
        var duplicatesNumberOfBoatsPerType = GetAmountPerUniqueBoat(duplicates, "duplicates");
        //        int capacity = duplicatesNumberOfBoatsPerType.getFirst().capacity;
        ArrayList<UniqueBoatsInformation> allNumberOfBoatsPerTypeTogether = new ArrayList<UniqueBoatsInformation>();
        allNumberOfBoatsPerTypeTogether.addAll(jsonNumberOfBoatsPerType);
        allNumberOfBoatsPerTypeTogether.addAll(excelNumberOfBoatsPerType);
        allNumberOfBoatsPerTypeTogether.addAll(duplicatesNumberOfBoatsPerType);

        var onlyUniqueBoatTypes = GetUniqueBoatTypes(allNumberOfBoatsPerTypeTogether);
//        System.out.println("alle unieke boten " + onlyUniqueBoatTypes);


        List<BoatReservation> boatReservations = GetListOfBoatReservations();

        for (int i = 0; i < onlyUniqueBoatTypes.size(); i++) {
            int amountFoundInJson = SearchAmountOfBoats(onlyUniqueBoatTypes.get(i), jsonNumberOfBoatsPerType);
            int amountFoundInExcel = SearchAmountOfBoats(onlyUniqueBoatTypes.get(i), excelNumberOfBoatsPerType);
//            System.out.println("in Json hebben we van " + onlyUniqueBoatTypes.get(i) + " er " + amountFoundInJson + " gevonden");
//            System.out.println("in Excel hebben we van " + onlyUniqueBoatTypes.get(i) + " er " + amountFoundInExcel + " gevonden");
            int amountNeedToAdd = amountFoundInExcel - amountFoundInJson;
//            System.out.println("need to add : " + amountNeedToAdd + " of " + onlyUniqueBoatTypes.get(i));
            if (amountNeedToAdd < 0) {
                int amountToDelete = amountNeedToAdd * -1;

                //retourneerd alle boten in de json van uniqueBoat
                List<Boat> allJsonBoatsWithUniqueBoat = GetAllJsonBoatsWithUniqueBoat(onlyUniqueBoatTypes.get(i), jsonBoats);
//                System.out.println("de boten die hieronder vallen zijn: " + allJsonBoatsWithUniqueBoat);
                List<Integer> idsThatCanBeRemoved = new ArrayList<>();
                List<Integer> idListThatHasReservationsInFuture = new ArrayList<>();

                for (int j = 0; j < allJsonBoatsWithUniqueBoat.size(); j++) {


                    var boat = allJsonBoatsWithUniqueBoat.get(j);
                    //checken op of hij een reservatie heeft die in de toekomst plaatsvindt
                    boolean hasReservationInTheFuture = HasReservationInTheFuture(boat);
                    if (hasReservationInTheFuture) {
//                        System.out.println("Boat with ID: " + boat.uniqueId() + " heeft wel een reservatie in de toekomst");
                        idListThatHasReservationsInFuture.add(boat.uniqueId());
                    } else {
//                        System.out.println("Boat with ID: " + boat.uniqueId() + " geen reservatie in de toekomst");
                        idsThatCanBeRemoved.add(boat.uniqueId());
                    }
                }
                if (idsThatCanBeRemoved.size() < amountToDelete) {
                    System.out.println("Er moeten in totaal " + (amountToDelete - idsThatCanBeRemoved.size()) + " nog verwijderd worden voordat de update kan doorgevoert worden");
                    for (int k = 0; k < idListThatHasReservationsInFuture.size(); k++) {
                        int uniqueId = idListThatHasReservationsInFuture.get(k);
                        System.out.println("Je kan ervoor kiezen om de reserveringen op boot idnummer: " + uniqueId + " te verwijderen");
                    }
                    throw new RuntimeException("er zijn te veel boten nog in gebruik in de toekomst om de boat lijst te kunnen updaten");

                }
                idsThatCanBeRemoved.sort(Collections.reverseOrder());
                for (int j = 0; j < amountToDelete; j++) {
                    Boat boat = BoatService.read(idsThatCanBeRemoved.get(j));
                    boatsToDelete.add(boat);
                }
//                System.out.println("amountToDelete is: " + amountToDelete);


                //check welke boten verwijderd mogen worden door:
                // te kijken welke boten geen bootreservatie na het heden hebben.

            }

//            System.out.println();

        }
//        System.out.println();


        return boatsToDelete;
    }

    private static boolean HasReservationInTheFuture(Boat boat) {
        //haalt een lijst op van alle bootreserveringen die in de toekomst zijn
        boolean hasReservationInTheFuture = false;
        var allFutureBoatReservations = GetAllFutureBoatReservations();

        for (int i = 0; i < allFutureBoatReservations.size(); i++) {
            if (boat.uniqueId() == allFutureBoatReservations.get(i).boat().uniqueId()) {
                hasReservationInTheFuture = true;
                break;
            }
        }

        return hasReservationInTheFuture;
    }

    private static List<BoatReservation> GetAllFutureBoatReservations() {
        List<BoatReservation> boatReservationsThatAreInTheFuture = new ArrayList<>();
        List<BoatReservation> boatReservations = GetListOfBoatReservations();

        for (int i = 0; i < boatReservations.size(); i++) {
            BoatReservation boatReservation = boatReservations.get(i);
            if (boatReservation.beginDateTime().isAfter(LocalDateTime.now())) {
                boatReservationsThatAreInTheFuture.add(boatReservation);
            }
        }
        return boatReservationsThatAreInTheFuture;
    }

    private static List<Boat> GetAllJsonBoatsWithUniqueBoat(UniqueBoat uniqueBoat, List<Boat> jsonBoats) {
        List<Boat> allJsonBoatsWithUniqueBoat = new ArrayList<>();

        for (int i = 0; i < jsonBoats.size(); i++) {
            if (uniqueBoat.Type.equals(jsonBoats.get(i).type()) && uniqueBoat.capacity == jsonBoats.get(i).capacity()) {
                allJsonBoatsWithUniqueBoat.add(jsonBoats.get(i));
            }
        }
        return allJsonBoatsWithUniqueBoat;
    }

    private static List<BoatReservation> GetListOfBoatReservations() {
        var customerReservations = new ReservationRepo().load();
        List<BoatReservation> listBoatReservations = new ArrayList<>();

        for (int i = 0; i < customerReservations.size(); i++) {
            CustomerReservation customerReservation = customerReservations.get(i);
            var boatReservations = customerReservation.boat();
            for (int j = 0; j < boatReservations.length; j++) {
                BoatReservation boatReservation = boatReservations[j];
                listBoatReservations.add(boatReservation);
            }
        }
        return listBoatReservations;
    }

    private static List<Boat> getNewEntries(List<Boat> jsonBoats, List<Boat> excelBoats, List<Boat> duplicates) {

        List<Boat> listOfNewEntries = new ArrayList<>();

        var jsonNumberOfBoatsPerType = GetAmountPerUniqueBoat(jsonBoats, "jsonBoats");
        var excelNumberOfBoatsPerType = GetAmountPerUniqueBoat(excelBoats, "excelBoats");
        var duplicatesNumberOfBoatsPerType = GetAmountPerUniqueBoat(duplicates, "duplicates");
        // int capacity = duplicatesNumberOfBoatsPerType.getFirst().capacity;
        ArrayList<UniqueBoatsInformation> allNumberOfBoatsPerTypeTogether = new ArrayList<UniqueBoatsInformation>();
        allNumberOfBoatsPerTypeTogether.addAll(jsonNumberOfBoatsPerType);
        allNumberOfBoatsPerTypeTogether.addAll(excelNumberOfBoatsPerType);
        allNumberOfBoatsPerTypeTogether.addAll(duplicatesNumberOfBoatsPerType);

        var onlyUniqueBoatTypes = GetUniqueBoatTypes(allNumberOfBoatsPerTypeTogether);
//        System.out.println("alle unieke boten " + onlyUniqueBoatTypes);


        for (int i = 0; i < onlyUniqueBoatTypes.size(); i++) {
            int amountFoundInJson = SearchAmountOfBoats(onlyUniqueBoatTypes.get(i), jsonNumberOfBoatsPerType);
            int amountFoundInExcel = SearchAmountOfBoats(onlyUniqueBoatTypes.get(i), excelNumberOfBoatsPerType);
//            System.out.println("in Json hebben we van " + onlyUniqueBoatTypes.get(i) + " er " + amountFoundInJson + " gevonden");
//            System.out.println("in Excel hebben we van " + onlyUniqueBoatTypes.get(i) + " er " + amountFoundInExcel + " gevonden");
            int amountNeedToAdd = amountFoundInExcel - amountFoundInJson;
//            System.out.println("need to add : " + amountNeedToAdd + " of " + onlyUniqueBoatTypes.get(i));
            for (int j = 0; j < amountNeedToAdd; j++) {
                listOfNewEntries.add(BoatService.create(onlyUniqueBoatTypes.get(i).Type, onlyUniqueBoatTypes.get(i).capacity, onlyUniqueBoatTypes.get(i).properties(),onlyUniqueBoatTypes.get(i).pricePer15Min,onlyUniqueBoatTypes.get(i).pricePer30Min));
            }
//            System.out.println();

        }
//        System.out.println();


        return listOfNewEntries;
    }

    private static int SearchAmountOfBoats(UniqueBoat uniqueBoat, List<UniqueBoatsInformation> numberOfBoatsPerType) {
        int amountOfBoats = 0;

        for (int i = 0; i < numberOfBoatsPerType.size(); i++) {
            if ((numberOfBoatsPerType.get(i).boatType.equals(uniqueBoat.Type)) && (numberOfBoatsPerType.get(i).capacity == uniqueBoat.capacity)) {
                amountOfBoats = numberOfBoatsPerType.get(i).amountOfThisBoat();
            }
        }

        return amountOfBoats;
    }

    private static List<UniqueBoat> GetUniqueBoatTypes (ArrayList<UniqueBoatsInformation> allNumberOfBoatsPerTypeTogether) {
        List<UniqueBoat> onlyBoats = new ArrayList<>();
        List<UniqueBoat> onlyUniqueBoats = new ArrayList<>();


        for (int i = 0; i < allNumberOfBoatsPerTypeTogether.size(); i++) {
            String boatType = allNumberOfBoatsPerTypeTogether.get(i).boatType;
            int boatCapacity = allNumberOfBoatsPerTypeTogether.get(i).capacity;
            String[] boatProperties = allNumberOfBoatsPerTypeTogether.get(i).properties;
            var boatPricePer15M = allNumberOfBoatsPerTypeTogether.get(i).pricePer15Min;
            var boatPricePer30M = allNumberOfBoatsPerTypeTogether.get(i).pricePer30Min;
            UniqueBoat uniqueBoat = new UniqueBoat(boatType, boatCapacity, boatProperties,boatPricePer15M,boatPricePer30M);
            onlyBoats.add(uniqueBoat);
        }
        onlyBoats.sort(Comparator.comparing(UniqueBoat::Type).thenComparing(UniqueBoat::capacity).thenComparing(b -> Arrays.toString(b.properties())));
        for (int i = 0; i < onlyBoats.size(); i++) {
            UniqueBoat currentUniqueBoat;
            UniqueBoat oldUniqueBoat;
            if (i == 0) {
                currentUniqueBoat = onlyBoats.get(i);
                oldUniqueBoat = onlyBoats.get(i);
            } else {
                currentUniqueBoat = onlyBoats.get(i);
                oldUniqueBoat = onlyBoats.get(i - 1);
            }

            if (!currentUniqueBoat.equals(oldUniqueBoat) && i != allNumberOfBoatsPerTypeTogether.size() - 1) {
                onlyUniqueBoats.add(oldUniqueBoat);
            }
            if (i == allNumberOfBoatsPerTypeTogether.size() - 1) {
                //                System.out.println("laatste ronde");
                onlyUniqueBoats.add(oldUniqueBoat);
                if (!oldUniqueBoat.equals(currentUniqueBoat)) {
                    onlyUniqueBoats.add(currentUniqueBoat);
                }

            }
            //            System.out.println(i + "currentUniqueBoat: " + currentUniqueBoat);
            //            System.out.println(i + "oldUniqueBoat: " + oldUniqueBoat);
            //            System.out.println(i + "onlyUniqueBoats: " + onlyUniqueBoats);
            //            System.out.println();
        }
        return onlyUniqueBoats;
    }

    private static List<Boat> GetDuplicatesJsonExcel(List<Boat> jsonBoats, List<Boat> excelBoats) {
        List<Boat> duplicates = new ArrayList<>();
        for (int i = 0; i < jsonBoats.size(); i++) {
            for (int j = 0; j < excelBoats.size(); j++) {
                if (boatsAreEqual(jsonBoats.get(i), excelBoats.get(j))) {
                    duplicates.add(jsonBoats.get(i));

                    break;
                }
            }
        }
        return duplicates;
    }

    private static void SaveToJson(List<Boat> boatsToPutInJson) {
        BoatRepo boatRepo = new BoatRepo();
        boatRepo.save(boatsToPutInJson);
    }

    private static List<Boat> GetAllExcelBoats() {
        // de excel data ophalen

        return new BoatExcelRepo().load();
    }

    private static ArrayList<Boat> getExistingBoatsFromJson() {
        Comparator<Boat> comparator = Comparator.comparing(Boat::type).thenComparing(Boat::capacity).thenComparing(b -> Arrays.toString(b.properties())).thenComparing(Boat::pricePer15Min).thenComparing(Boat::pricePer30Min);
        List<Boat> jsonBoats = new BoatRepo().load();
        ArrayList<Boat> jsonFileArrayList = new ArrayList<>(jsonBoats);
        jsonFileArrayList.sort(comparator);
        return jsonFileArrayList;

    }

    private static boolean boatsAreEqual(Boat boatA, Boat boatB) {
        return boatA.capacity() == boatB.capacity() && boatA.type().equals(boatB.type());
    }

    private static void PrintBoatList(List<Boat> boatList, String type) {
        System.out.println(boatList.size() + " : " + type + " " + boatList);

    }

    private static List<UniqueBoatsInformation> GetAmountPerUniqueBoat(List<Boat> excelBoats, String boatType) {
        List<UniqueBoatsInformation> listOfAmountPerUniqueBoat = new ArrayList<>();
        int counter = 1;

        Boat nextBoat;
        Boat currentBoat;

        //        System.out.println("de grootte van de " + boatType + " is: " + excelBoats.size());
        //        System.out.println();

        boolean isLastBoat = false;
        for (int i = 0; i < excelBoats.size(); i++) {
            currentBoat = excelBoats.get(i);

            if (i == excelBoats.size() - 1) {
                nextBoat = excelBoats.getLast();

                isLastBoat = true;
            } else {
                nextBoat = excelBoats.get(i + 1);
            }

            if ((!boatsAreEqual(nextBoat, currentBoat)) || isLastBoat) {
                var uniqueBoatsInformation = new UniqueBoatsInformation(counter, currentBoat.type(), currentBoat.capacity(), currentBoat.properties(),currentBoat.pricePer15Min(),currentBoat.pricePer30Min());
                listOfAmountPerUniqueBoat.add(uniqueBoatsInformation);
                //                System.out.println("Resultaat: " + uniqueBoatsInformation);
                counter = 1;
                isLastBoat = false;
            } else {
                counter++;
            }

        }
        return listOfAmountPerUniqueBoat;
    }

    public record UniqueBoatsInformation(int amountOfThisBoat, String boatType, int capacity, String[] properties, double pricePer15Min, double pricePer30Min) {
    }

    public record UniqueBoat(String Type, int capacity, String[] properties, double pricePer15Min, double pricePer30Min) {

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof UniqueBoat other))
                return false;

            return capacity == other.capacity && Type.equals(other.Type);
        }

        @Override
        public int hashCode() {
            return Type.hashCode() + Integer.hashCode(capacity);
        }
    }

}
package com.capgemini.freebirds.data;

import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.model.BoatReservation;
import com.capgemini.freebirds.model.CustomerReservation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BoatReservationRepo implements BoatReservationRepoInterface {

    public void save(List<BoatReservation> reservationList) {
        Path path = FileSystems.getDefault().getPath("boatReservations.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            objectMapper.writeValue(new File(path.toUri()), reservationList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BoatReservation> load() {
        Path path = FileSystems.getDefault().getPath( "boatReservations.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        List<BoatReservation> reservationList = new ArrayList<>();
        try {
            reservationList = objectMapper.readValue(new File(path.toUri()), new TypeReference<ArrayList<BoatReservation>>() {});
            return reservationList;
        } catch (IOException e) {
            System.out.println("File was empty, added empty list to file");
            return reservationList;
        }
    }

    public void createFile() {
        Path path = FileSystems.getDefault().getPath("boatReservations.json");
        File reservationJSON = new File(path.toUri());

        try {
            if (reservationJSON.createNewFile()) {
                System.out.println("File created:" + reservationJSON.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("an error occurred");
            e.printStackTrace();
        }
    }
}

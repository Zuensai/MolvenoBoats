package com.capgemini.freebirds.data;

import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.model.Customer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BoatRepo implements BoatRepoInterface {


    public List<Boat> load() {

        Path path = FileSystems.getDefault().getPath("boatJSON.json");
        ObjectMapper objectMapper = new ObjectMapper();

        List<Boat> boatList = new ArrayList<>();
        try {
            boatList = objectMapper.readValue(new File(path.toUri()), new TypeReference<ArrayList<Boat>>() {});
            return boatList;
        }
        catch (IOException e) {
            System.out.println("File was empty, added empty list to file");
            return boatList;
        }


    }

    public void save(List<Boat> boatList) {

        Path path = FileSystems.getDefault().getPath("boatJSON.json");
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writeValue(new File(path.toUri()), boatList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void createFile() {

        Path path = FileSystems.getDefault().getPath("boatJSON.json");
        File boatJSON = new File(path.toUri());

        try {
            if (boatJSON.createNewFile()) {
                System.out.println("File created:" + boatJSON.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("an error occurred");
            e.printStackTrace();
        }
    }
}

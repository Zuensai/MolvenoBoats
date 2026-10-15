package com.capgemini.freebirds.data;

import com.capgemini.freebirds.model.Customer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepo implements CustomerRepoInterface {

    public void save(List<Customer> customerList) {

        Path path = FileSystems.getDefault().getPath("customerJSON.json");
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            objectMapper.writeValue(new File(path.toUri()), customerList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public List<Customer> load() {

        Path path = FileSystems.getDefault().getPath("customerJSON.json");
        ObjectMapper objectMapper = new ObjectMapper();

        List<Customer> customerList = new ArrayList<>();
        try {
            customerList = objectMapper.readValue(new File(path.toUri()), new TypeReference<ArrayList<Customer>>() {});
            return customerList;
        }
        catch (IOException e) {
            System.out.println("File was empty, added empty list to file");
            return customerList;
        }

    }


    public void createFile() {

        Path path = FileSystems.getDefault().getPath("customerJSON.json");
        File customerJSON = new File(path.toUri());

        try {
            if (customerJSON.createNewFile()) {
                System.out.println("File created:" + customerJSON.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("an error occurred");
            e.printStackTrace();
        }
    }
}

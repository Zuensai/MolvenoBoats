package com.capgemini.freebirds.service;

import java.util.List;
import com.capgemini.freebirds.model.Customer;

public interface CustomerServiceInterface {

    Customer create(int id, String firstName, String lastName, String phoneNumber, String mailAddress);
    Customer read(int id);
    void update(Customer customer);
    void delete(Customer customer);
    List<Customer> getAll();
}

package com.capgemini.freebirds.data;

import com.capgemini.freebirds.model.Customer;

public class CustomerReceiver {

    public static Customer receive(int uniqueCustomerId) {

        //customer initialisers
        Customer customer = new Customer(uniqueCustomerId, "Jan", "Modaal", "0612345678", "janmodaal@gmail.com");

        return customer;
    }
}

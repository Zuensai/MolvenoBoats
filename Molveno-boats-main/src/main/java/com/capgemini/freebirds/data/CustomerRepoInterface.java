package com.capgemini.freebirds.data;

import com.capgemini.freebirds.model.Customer;

import java.util.List;

public interface CustomerRepoInterface {

    public List<Customer> load();

    public void save(List<Customer> customerList);

}

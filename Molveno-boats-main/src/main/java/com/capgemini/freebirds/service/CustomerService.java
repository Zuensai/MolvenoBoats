package com.capgemini.freebirds.service;

import com.capgemini.freebirds.data.CustomerRepo;
import com.capgemini.freebirds.data.CustomerRepoInterface;
import com.capgemini.freebirds.model.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CustomerService implements CustomerServiceInterface {

    CustomerRepoInterface customerRepo = new CustomerRepo();
    List<Customer> customerList = customerRepo.load();

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public Customer create(int id, String firstName, String lastName, String phoneNumber, String mailAddress) {

        Customer newCustomer = new Customer(id, firstName, lastName, phoneNumber, mailAddress);
        if (Objects.equals(read(id), newCustomer)) {
            System.out.println("Customer already exists!");
            return null;
        }
        customerList.add(newCustomer);
        customerRepo.save(customerList); //maybe vergeten? zag em bij BoatService wel staan. zelf ff bijgezet.
        return newCustomer;
    }

    public Customer read(int id) {

        for (Customer customer : customerList) {

            if (customer == null) {
                System.out.println("Database is empty!");
                return null;
            }
            if (customer.uniqueCustomerId() == id) {

                String cusName = customer.firstName();
                String cusSurname = customer.lastName();
                String cusPhoneNum = customer.phoneNumber();
                String cusMailAddress = customer.mailAddress();

                return new Customer(id, cusName, cusSurname, cusPhoneNum, cusMailAddress);
            }

        }
        return null;


    }


    public Customer read(String lastName) {

        for (Customer customer : customerList) {

            if (Objects.equals(customer.lastName(), lastName)) {


                int id = customer.uniqueCustomerId();
                String firstName = customer.firstName();
                String cusSurname = customer.lastName();
                String cusPhoneNum = customer.phoneNumber();
                String cusMailAddress = customer.mailAddress();

                return new Customer(id, firstName, cusSurname, cusPhoneNum, cusMailAddress);
            }

        }
        return null;
    }

    public boolean ifExists(Customer customerToCheck) {

        for (Customer customerInList : customerList) {

            if (customerInList == customerToCheck) {
                return true;
            }

        }
        return false;
    }

    public void update(Customer customer) {

        List<Customer> toRemove = new ArrayList<>();

        for (Customer customers : customerList) {
            if (customer == null) {
                break;
            } else if (customer.uniqueCustomerId() == customers.uniqueCustomerId()) {
                toRemove.add(customers);
            }

        }

        customerList.removeAll(toRemove);
        customerList.add(customer);

        while (customerList.contains(null)) {
            customerList.remove(null);
        }
        customerRepo.save(customerList);
    }

    public void delete(Customer customerToDelete) {
        customerList.remove(customerToDelete);
        customerRepo.save(customerList);
    }

    public List<Customer> getAll() {
        return customerList;
    }
}


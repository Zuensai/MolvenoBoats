package com.capgemini.freebirds.service;

import java.time.LocalDateTime;
import java.util.List;
import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.model.Customer;
import com.capgemini.freebirds.model.BoatReservation;
import com.capgemini.freebirds.model.CustomerReservation;

public interface ReservationServiceInterface {

    CustomerReservation create(Customer customer, BoatReservation[] boats, LocalDateTime beginDateTime, LocalDateTime endDateTime);
    CustomerReservation read(int id);
    CustomerReservation update(CustomerReservation reservation);
    void delete(CustomerReservation reservation);
    List<CustomerReservation> getAll();
}

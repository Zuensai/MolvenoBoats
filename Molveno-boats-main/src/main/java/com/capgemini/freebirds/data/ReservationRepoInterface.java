package com.capgemini.freebirds.data;

import com.capgemini.freebirds.model.CustomerReservation;

import java.util.List;

public interface ReservationRepoInterface {

    public void save(List<CustomerReservation> reservationList);

    public List<CustomerReservation> load();

    public void createFile();
}

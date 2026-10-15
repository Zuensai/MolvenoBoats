package com.capgemini.freebirds.data;

import com.capgemini.freebirds.model.BoatReservation;
import com.capgemini.freebirds.model.Customer;

import java.util.List;

public interface BoatReservationRepoInterface {

    public List<BoatReservation> load();

    public void save(List<BoatReservation> BoatReservationList);

}

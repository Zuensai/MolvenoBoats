package com.capgemini.freebirds.data;

import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.model.Customer;

import java.util.List;

public interface BoatRepoInterface {

    public List<Boat> load();

    public void save(List<Boat> boatList);

}

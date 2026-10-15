package com.capgemini.freebirds;

import com.capgemini.freebirds.data.BoatRepo;
import com.capgemini.freebirds.data.BoatReservationRepo;
import com.capgemini.freebirds.data.CustomerRepo;
import com.capgemini.freebirds.data.ReservationRepo;
import com.capgemini.freebirds.data.UniqueIdFileController;
import com.capgemini.freebirds.presentation.SwingUi;
import com.capgemini.freebirds.presentation.UserInterface;
import com.capgemini.freebirds.service.*;


import java.util.List;

public class Main {
    public static void main(String[] args) {
        BoatRepo boatRepo = new BoatRepo();
        boatRepo.createFile();

        UniqueIdFileController uniqueIdFileController = new UniqueIdFileController();
        uniqueIdFileController.createFile();

        CustomerRepo customerRepo = new CustomerRepo();
        customerRepo.createFile();

        ReservationRepo reservationRepo = new ReservationRepo();
        reservationRepo.createFile();

        BoatReservationRepo boatReservationRepo = new BoatReservationRepo();
        boatReservationRepo.createFile();

        CustomerService customerService = new CustomerService();
        BoatService boatService = new BoatService();

        int nextNumber = UniqueIdCreator.getNextUniqueId();
        System.out.println("Next number is: " + nextNumber);

        UserInterface ui = new SwingUi(boatService, customerService);
        ui.start();

    }
}
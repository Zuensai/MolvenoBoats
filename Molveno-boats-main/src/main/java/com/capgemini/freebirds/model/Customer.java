package com.capgemini.freebirds.model;

public record Customer(int uniqueCustomerId, String firstName, String lastName, String phoneNumber, String mailAddress) {}

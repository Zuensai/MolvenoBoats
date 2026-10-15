package com.capgemini.freebirds.model;

public record Boat(int uniqueId, String type, int capacity, String[] properties, double pricePer15Min, double pricePer30Min) {}

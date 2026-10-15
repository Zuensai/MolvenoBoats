package com.capgemini.freebirds.data;

import com.capgemini.freebirds.model.Boat;
import com.capgemini.freebirds.service.UniqueIdCreator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BoatExcelRepo implements BoatRepoInterface{

    Path path = FileSystems.getDefault().getPath("Bootverhuur_Prices.xlsx");
    FileInputStream file;
    Workbook workbook;

    {
        try {
            file = new FileInputStream(new File(path.toUri()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    {
        try {
            workbook = new XSSFWorkbook(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<HashMap<String, Object>> loadFromFile() {

        Sheet sheet = workbook.getSheetAt(0);
        List<HashMap<String, Object>> allExcelBoats = new ArrayList<>();

        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue;
            HashMap<String, Object> boatProperties = new HashMap<>();

            for (Cell cell : row) {

                switch(cell.getColumnIndex()) {
                    case 0: {
                        boatProperties.put("boatID", cell.getStringCellValue());
                        break;
                    }
                    case 1: {
                        boatProperties.put("boatType", cell.getStringCellValue());
                        break;
                    }
                    case 2: {
                        boatProperties.put("numberOfBoats", cell.getNumericCellValue());
                        break;
                    }
                    case 3: {
                        boatProperties.put("capacity", cell.getNumericCellValue());
                        break;
                    }
                    case 4: {
                        boatProperties.put("LakeArea", cell.getStringCellValue());
                        break;
                    }
                    case 5: {
                        boatProperties.put("isElectric", cell.getStringCellValue());
                        break;
                    }
                    case 6: {
                        boatProperties.put("hasLifeJackets", cell.getStringCellValue());
                        break;
                    }
                    case 7: {
                        boatProperties.put("comments", cell.getStringCellValue());
                        break;
                    }
                    case 8: {
                        boatProperties.put("pricePer15", cell.getNumericCellValue());
                        break;
                    }
                    case 9: {
                        boatProperties.put("pricePer30", cell.getNumericCellValue());
                        break;
                    }
                }
            }
            allExcelBoats.add(boatProperties);
        }
        return allExcelBoats;
    }


    public List<Boat> load() {
        List<HashMap<String, Object>> allExcelBoats = loadFromFile();
        List<Boat> allBoats = new ArrayList<>();

        for (HashMap<String, Object> boats : allExcelBoats) {
            int i = 0;
            Double numberOfBoats = (double)boats.get("numberOfBoats");
            int numberOfBoatsAsInt = numberOfBoats.intValue();

                while (numberOfBoatsAsInt > i) {

                    String boatID = (String)boats.get("boatID");
                    String boatType = (String)boats.get("boatType");
                    String LakeArea = (String)boats.get("LakeArea");
                    String isElectric = (String)boats.get("isElectric");
                    String hasLifeJackets = (String)boats.get("hasLifeJackets");
                    String comments = (String)boats.get("comments");
                    double pricePer15 = (double)boats.get("pricePer15");
                    double pricePer30 = (double)boats.get("pricePer30");

                    Double capacity = (double)boats.get("capacity");
                    int capacityAsInt = capacity.intValue();

                    String[] boatProperties = new String[]{boatID, LakeArea, isElectric, hasLifeJackets, comments};

                    Boat newBoat = new Boat(0, boatType, capacityAsInt, boatProperties, pricePer15, pricePer30);
                    allBoats.add(newBoat);
                    i++;
                }
        }
        return allBoats;
    }

    public void save(List<Boat> boatList) {

    }
}

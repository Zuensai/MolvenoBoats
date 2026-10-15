package com.capgemini.freebirds.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class UniqueIdFileController {
    Path fileNamePath = Paths.get("uniqueIdSaveFile.txt");

    public int getHighestUniqueId() {
        File myObj = new File(fileNamePath.toUri());
        try (Scanner myReader = new Scanner(myObj)) {
            while (myReader.hasNextLine()) {
                int data = Integer.parseInt(myReader.nextLine());
                return data;
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return 0;
    }


    public void write(int newHighestId) {
        try {
            FileWriter myWriter;
            myWriter = new FileWriter(fileNamePath.toFile());
            myWriter.write(Integer.toString(newHighestId));
            myWriter.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void createFile() {

        File uniqueIdSaveFile = new File(fileNamePath.toUri());

        try {
            if (uniqueIdSaveFile.createNewFile()) {
                System.out.println("File created:" + uniqueIdSaveFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("an error occurred");
            e.printStackTrace();
        }

    }
}


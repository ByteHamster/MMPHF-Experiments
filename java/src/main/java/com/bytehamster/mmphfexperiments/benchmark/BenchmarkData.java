package com.bytehamster.mmphfexperiments.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class BenchmarkData {
    public static List<String> loadFile(String filename, int maxStrings) {
        System.out.println("Loading input file");
        List<String> inputData = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                inputData.add(scanner.nextLine());
                if (inputData.size() >= maxStrings) {
                    break;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Sorting input data");
        Collections.sort(inputData);
        System.out.println("Loaded " + inputData.size() + " strings");
        return Collections.unmodifiableList(inputData);
    }
}

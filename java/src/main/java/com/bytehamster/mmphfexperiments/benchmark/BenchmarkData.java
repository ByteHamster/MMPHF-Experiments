package com.bytehamster.mmphfexperiments.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class BenchmarkData {
    public static List<String> loadStringFile(String filename, int maxStrings) {
        System.out.println("Loading input file");
        List<String> inputData = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!inputData.isEmpty()) {
                    if (inputData.get(inputData.size() - 1).compareTo(line) > 0) {
                        throw new RuntimeException("Not sorted or duplicate key");
                    }
                }
                inputData.add(line);
                if (inputData.size() >= maxStrings) {
                    break;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Loaded " + inputData.size() + " strings");
        return Collections.unmodifiableList(inputData);
    }

    public static List<Long> loadInt64File(String filename, int maxInts) {
        List<Long> inputData = new ArrayList<>();
        try (LongStream fis = new LongStream(Files.newInputStream(Paths.get(filename)))) {
            long n = fis.readLong();
            System.out.println("Loading input file of size " + n);
            for (int i = 0; i < n; i++) {
                long l = fis.readLong() + Long.MIN_VALUE;
                inputData.add(l);
                if (inputData.size() >= maxInts) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Loaded " + inputData.size() + " integers");
        return Collections.unmodifiableList(inputData);
    }

    public static List<Long> loadInt32File(String filename, int maxInts) {
        List<Long> inputData = new ArrayList<>();
        try (IntStream fis = new IntStream(Files.newInputStream(Paths.get(filename)))) {
            int n = fis.readInt();
            System.out.println("Loading input file of size " + n);
            for (int i = 0; i < n; i++) {
                long l = fis.readInt();
                inputData.add(l);
                if (inputData.size() >= maxInts) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Loaded " + inputData.size() + " integers");
        return Collections.unmodifiableList(inputData);
    }
}

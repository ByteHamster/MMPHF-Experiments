package com.bytehamster.mmphfexperiments.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Contender {
    public static int numQueries = 100000;
    public static String dataset = "unknown";

    abstract String name();
    abstract long sizeBits();
    abstract void construct(List<String> keys);

    void beforeConstruction(List<String> keys) { }
    abstract long performQuery(String key);

    public void run(List<String> keys) {
        int N = keys.size();
        System.out.println("Contender: " + (name().contains(" ") ? name().substring(0, name().indexOf(' ')) : name()));
        beforeConstruction(keys);

        System.out.println("Cooldown");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Constructing");

        long begin = System.currentTimeMillis();
        construct(keys);
        long end = System.currentTimeMillis();
        long constructionTime = end - begin;

        System.out.println("Testing");
        doPerformTest(keys);

        long queryTime = 0;
        if (numQueries > 0) {
            System.out.println("Preparing query plan");
            List<String> queryPlan = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < numQueries; i++) {
                queryPlan.add(keys.get(random.nextInt(N)));
            }
            System.out.println("Cooldown");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Querying");
            begin = System.currentTimeMillis();
            doPerformQueries(queryPlan);
            end = System.currentTimeMillis();
            queryTime = end - begin;
        }
        double bitsPerElement = (double) sizeBits() / N;
        System.out.println("RESULT"
                + " dataset=" + dataset
                + " name=" + name()
                + " bitsPerElement=" + bitsPerElement
                + " constructionTimeMilliseconds=" + constructionTime
                + " queryTimeMilliseconds=" + queryTime
                + " numQueries=" + numQueries
                + " N=" + N);
    }

    void doPerformQueries(List<String> keys) {
        for (String key : keys) {
            performQuery(key);
        }
    }

    void doPerformTest(List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            long retrieved = performQuery(keys.get(i));
            if (retrieved != i) {
                System.out.println("Error: Key at index " + i + " is not monotone minimal perfect (output: " + retrieved + ")");
                throw new RuntimeException("Not MMPHF");
            }
        }
    }
}

package com.bytehamster.mmphfexperiments.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Contender<T> {
    public static int numQueries = 100000;
    public static String dataset = "unknown";

    abstract String name();
    abstract long sizeBits();
    abstract void construct(List<T> keys);

    void beforeConstruction(List<T> keys) { }
    abstract long performQuery(T key);

    public void run(List<T> keys) {
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

        long constructionTime;
        try {
            long begin = System.currentTimeMillis();
            construct(keys);
            long end = System.currentTimeMillis();
            constructionTime = end - begin;
        } catch (Throwable t) {
            t.printStackTrace();
            return;
        }
        System.out.println("Testing");
        doPerformTest(keys);

        long queryTime = 0;
        if (numQueries > 0) {
            System.out.println("Preparing query plan");
            List<T> queryPlan = new ArrayList<>();
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
            long begin = System.currentTimeMillis();
            doPerformQueries(queryPlan);
            long end = System.currentTimeMillis();
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

    void doPerformQueries(List<T> keys) {
        for (T key : keys) {
            performQuery(key);
        }
    }

    void doPerformTest(List<T> keys) {
        for (int i = 0; i < keys.size(); i++) {
            long retrieved = performQuery(keys.get(i));
            if (retrieved != i) {
                System.out.println("Error: Key at index " + i + " is not monotone minimal perfect (output: " + retrieved + ")");
                throw new RuntimeException("Not MMPHF");
            }
        }
    }
}

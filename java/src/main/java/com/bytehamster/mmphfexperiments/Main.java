package com.bytehamster.mmphfexperiments;

import com.bytehamster.mmphfexperiments.benchmark.BenchmarkData;
import com.bytehamster.mmphfexperiments.benchmark.Contender;
import com.bytehamster.mmphfexperiments.benchmark.HollowTrieContender;
import com.bytehamster.mmphfexperiments.benchmark.HollowTrieDistContender;
import com.bytehamster.mmphfexperiments.benchmark.LcpContender;
import com.bytehamster.mmphfexperiments.benchmark.PaCoTrieContender;
import com.bytehamster.mmphfexperiments.benchmark.TwoStepsLcpContender;
import com.bytehamster.mmphfexperiments.benchmark.VLLcpContender;
import com.bytehamster.mmphfexperiments.benchmark.VLPaCoTrieContender;
import com.bytehamster.mmphfexperiments.benchmark.ZFastTrieDistributorContender;

import java.util.List;

public class Main {
    private static void printUsage() {
        System.err.println("Usage: java -jar MmphfExperiments.jar [args]");
        System.err.println("-n: Truncate input file to only this number of strings if it is longer");
        System.err.println("--numQueries: Number of queries to perform");
        System.err.println("--filename: Input data set to load. List of strings, separated by newlines");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        int maxN = Integer.MAX_VALUE;
        String filename = "";

        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-n":
                    maxN = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "--numQueries":
                    Contender.numQueries = Integer.parseInt(args[i + 1]);
                    i++;
                    break;
                case "--filename":
                    filename = args[i + 1];
                    i++;
                    break;
                default:
                    printUsage();
                    return;

            }
        }

        List<String> input = BenchmarkData.loadFile(filename, maxN);
        if (input.size() < 2) {
            System.out.println("Input file does not contain strings");
            return;
        }

        new HollowTrieContender().run(input);
        new HollowTrieDistContender().run(input);
        new LcpContender().run(input);
        new PaCoTrieContender().run(input);
        //new TwoStepsLcpContender().run(input);
        new VLLcpContender().run(input);
        new VLPaCoTrieContender().run(input);
        //new ZFastTrieDistributorContender().run(input);
    }
}

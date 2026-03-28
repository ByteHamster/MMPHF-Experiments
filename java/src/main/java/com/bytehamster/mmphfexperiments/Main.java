package com.bytehamster.mmphfexperiments;

import com.bytehamster.mmphfexperiments.benchmark.BenchmarkData;
import com.bytehamster.mmphfexperiments.benchmark.Contender;
import com.bytehamster.mmphfexperiments.benchmark.HollowTrieContender;
import com.bytehamster.mmphfexperiments.benchmark.HollowTrieDistContender;
import com.bytehamster.mmphfexperiments.benchmark.MaterializingByteArrayTransformationStrategy;
import com.bytehamster.mmphfexperiments.benchmark.LcpContender;
import com.bytehamster.mmphfexperiments.benchmark.PaCoTrieContender;
import com.bytehamster.mmphfexperiments.benchmark.Sux4jSetup;
import com.bytehamster.mmphfexperiments.benchmark.TwoStepsLcpContender;
import com.bytehamster.mmphfexperiments.benchmark.VLLcpContender;
import com.bytehamster.mmphfexperiments.benchmark.VLPaCoTrieContender;
import com.bytehamster.mmphfexperiments.benchmark.ZFastTrieDistributorContender;
import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.bits.TransformationStrategy;

import java.io.File;
import java.util.List;

public class Main {
    private static void printUsage() {
        System.err.println("Usage: java -jar MmphfExperiments.jar [args]");
        System.err.println("-n: Truncate input file to only this number of strings if it is longer");
        System.err.println("--numQueries: Number of queries to perform");
        System.err.println("--filename: Input data set to load. Format depending on --type");
        System.err.println("--type: Input data set format. strings, int64, int32");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        int maxN = Integer.MAX_VALUE;
        String filename = "";
        String type = "strings";

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
                case "--type":
                    type = args[i + 1];
                    i++;
                    break;
                default:
                    printUsage();
                    return;

            }
        }

        Contender.dataset = new File(filename).getName();
        Sux4jSetup.setup();
        if (type.equals("strings")) {
            List<byte[]> input = BenchmarkData.loadStringFile(filename, maxN);
            if (input.size() < 2) {
                System.out.println("Input file does not contain strings");
                return;
            }
            TransformationStrategy<byte[]> mat = MaterializingByteArrayTransformationStrategy.getInstance();
            new HollowTrieContender<byte[]>(mat).run(input);
            new HollowTrieDistContender<byte[]>(mat).run(input);
            new LcpContender<byte[]>(TransformationStrategies.prefixFreeByteArray()).run(input);
            new PaCoTrieContender<byte[]>(TransformationStrategies.prefixFreeByteArray()).run(input);
            new TwoStepsLcpContender<byte[]>(TransformationStrategies.prefixFreeByteArray()).run(input);
            new VLLcpContender<byte[]>(TransformationStrategies.prefixFreeByteArray()).run(input);
            new VLPaCoTrieContender<byte[]>(TransformationStrategies.prefixFreeByteArray()).run(input);
            new ZFastTrieDistributorContender<byte[]>(TransformationStrategies.prefixFreeByteArray()).run(input);
        } else {
            List<Long> input;
            if (type.equals("int64")) {
                input = BenchmarkData.loadInt64File(filename, maxN);
            } else if (type.equals("int32")) {
                input = BenchmarkData.loadInt32File(filename, maxN);
            } else {
                printUsage();
                return;
            }
            if (input.size() < 2) {
                System.out.println("Input file does not contain integers");
                return;
            }
            new HollowTrieContender<Long>(TransformationStrategies.fixedLong()).run(input);
            new HollowTrieDistContender<Long>(TransformationStrategies.fixedLong()).run(input);
            new LcpContender<Long>(TransformationStrategies.fixedLong()).run(input);
            new PaCoTrieContender<Long>(TransformationStrategies.fixedLong()).run(input);
            new TwoStepsLcpContender<Long>(TransformationStrategies.fixedLong()).run(input);
            new VLLcpContender<Long>(TransformationStrategies.fixedLong()).run(input);
            new VLPaCoTrieContender<Long>(TransformationStrategies.fixedLong()).run(input);
            new ZFastTrieDistributorContender<Long>(TransformationStrategies.fixedLong()).run(input);
        }
    }
}

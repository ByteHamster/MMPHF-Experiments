package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.sux4j.mph.PaCoTrieDistributorMonotoneMinimalPerfectHashFunction;

import java.io.IOException;
import java.util.List;

public class PaCoTrieContender extends Contender {
    PaCoTrieDistributorMonotoneMinimalPerfectHashFunction<String> mphf;

    @Override
    String name() {
        return "PaCoTrieJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void construct(List<String> keys) {
        try {
            mphf = new PaCoTrieDistributorMonotoneMinimalPerfectHashFunction<>(keys, TransformationStrategies.prefixFreeUtf16());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    long performQuery(String key) {
        return mphf.getLong(key);
    }
}

package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategy;
import it.unimi.dsi.sux4j.mph.VLPaCoTrieDistributorMonotoneMinimalPerfectHashFunction;

import java.io.IOException;
import java.util.List;

public class VLPaCoTrieContender<T> extends Contender<T> {
    VLPaCoTrieDistributorMonotoneMinimalPerfectHashFunction<T> mphf;
    private final TransformationStrategy<T> strategy;

    public VLPaCoTrieContender(TransformationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    @Override
    String name() {
        return "VLPaCoTrieJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void construct(List<T> keys) {
        try {
            mphf = new VLPaCoTrieDistributorMonotoneMinimalPerfectHashFunction<>(keys, strategy);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    long performQuery(T key) {
        return mphf.getLong(key);
    }
}

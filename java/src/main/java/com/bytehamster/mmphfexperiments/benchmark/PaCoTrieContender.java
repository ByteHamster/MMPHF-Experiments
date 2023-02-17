package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategy;
import it.unimi.dsi.sux4j.mph.PaCoTrieDistributorMonotoneMinimalPerfectHashFunction;

import java.io.IOException;
import java.util.List;

public class PaCoTrieContender<T> extends Contender<T> {
    PaCoTrieDistributorMonotoneMinimalPerfectHashFunction<T> mphf;
    private final TransformationStrategy<T> strategy;

    public PaCoTrieContender(TransformationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    @Override
    String name() {
        return "PaCoTrieJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void construct(List<T> keys) {
        try {
            mphf = new PaCoTrieDistributorMonotoneMinimalPerfectHashFunction<>(keys, strategy);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    long performQuery(T key) {
        return mphf.getLong(key);
    }
}

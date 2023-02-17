package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategy;
import it.unimi.dsi.sux4j.mph.HollowTrieDistributorMonotoneMinimalPerfectHashFunction;

import java.io.IOException;
import java.util.List;

public class HollowTrieDistContender<T> extends Contender<T> {
    HollowTrieDistributorMonotoneMinimalPerfectHashFunction<T> mphf;
    private final TransformationStrategy<T> strategy;

    public HollowTrieDistContender(TransformationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    @Override
    String name() {
        return "HollowTrieDistJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void construct(List<T> keys) {
        try {
            mphf = new HollowTrieDistributorMonotoneMinimalPerfectHashFunction<>(keys, strategy);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    long performQuery(T key) {
        return mphf.getLong(key);
    }
}

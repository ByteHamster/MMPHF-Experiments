package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategy;
import it.unimi.dsi.sux4j.mph.HollowTrieMonotoneMinimalPerfectHashFunction;

import java.util.List;

public class HollowTrieContender<T> extends Contender<T> {
    HollowTrieMonotoneMinimalPerfectHashFunction<T> mphf;
    private final TransformationStrategy<T> strategy;

    public HollowTrieContender(TransformationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    @Override
    String name() {
        return "HollowTrieJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void construct(List<T> keys) {
        mphf = new HollowTrieMonotoneMinimalPerfectHashFunction<T>(keys, strategy);
    }

    @Override
    long performQuery(T key) {
        return mphf.getLong(key);
    }
}

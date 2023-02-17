package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategy;
import it.unimi.dsi.sux4j.mph.ZFastTrieDistributorMonotoneMinimalPerfectHashFunction;

import java.io.IOException;
import java.util.List;

public class ZFastTrieDistributorContender<T> extends Contender<T> {
    ZFastTrieDistributorMonotoneMinimalPerfectHashFunction.Builder<T> mphfBuilder;
    ZFastTrieDistributorMonotoneMinimalPerfectHashFunction<T> mphf;
    private final TransformationStrategy<T> strategy;

    public ZFastTrieDistributorContender(TransformationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    @Override
    String name() {
        return "ZFastTrieDistributorJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void beforeConstruction(List<T> keys) {
        mphfBuilder = new ZFastTrieDistributorMonotoneMinimalPerfectHashFunction.Builder<>();
        mphfBuilder.keys(keys);
        mphfBuilder.transform(strategy);
    }

    @Override
    void construct(List<T> keys) {
        try {
            mphf = mphfBuilder.build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    long performQuery(T key) {
        return mphf.getLong(key);
    }
}

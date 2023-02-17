package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategy;
import it.unimi.dsi.sux4j.mph.LcpMonotoneMinimalPerfectHashFunction;

import java.io.IOException;
import java.util.List;

public class LcpContender<T> extends Contender<T> {
    LcpMonotoneMinimalPerfectHashFunction.Builder<T> mphfBuilder;
    LcpMonotoneMinimalPerfectHashFunction<T> mphf;
    private final TransformationStrategy<T> strategy;

    public LcpContender(TransformationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    @Override
    String name() {
        return "LcpJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void beforeConstruction(List<T> keys) {
        mphfBuilder = new LcpMonotoneMinimalPerfectHashFunction.Builder<>();
        mphfBuilder.keys(keys);
        mphfBuilder.numKeys(keys.size());
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

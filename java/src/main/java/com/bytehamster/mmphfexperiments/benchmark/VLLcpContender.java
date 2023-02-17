package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategy;
import it.unimi.dsi.sux4j.mph.VLLcpMonotoneMinimalPerfectHashFunction;

import java.io.IOException;
import java.util.List;

public class VLLcpContender<T> extends Contender<T> {
    VLLcpMonotoneMinimalPerfectHashFunction<T> mphf;
    private final TransformationStrategy<T> strategy;

    public VLLcpContender(TransformationStrategy<T> strategy) {
        this.strategy = strategy;
    }

    @Override
    String name() {
        return "VLLcpJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void construct(List<T> keys) {
        try {
            mphf = new VLLcpMonotoneMinimalPerfectHashFunction<>(keys, strategy);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    long performQuery(T key) {
        return mphf.getLong(key);
    }
}

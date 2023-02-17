package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.sux4j.mph.TwoStepsLcpMonotoneMinimalPerfectHashFunction;

import java.io.IOException;
import java.util.List;

public class TwoStepsLcpContender extends Contender {
    TwoStepsLcpMonotoneMinimalPerfectHashFunction.Builder<String> mphfBuilder;
    TwoStepsLcpMonotoneMinimalPerfectHashFunction<String> mphf;

    @Override
    String name() {
        return "TwoStepsLcpJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void beforeConstruction(List<String> keys) {
        mphfBuilder = new TwoStepsLcpMonotoneMinimalPerfectHashFunction.Builder<>();
        mphfBuilder.keys(keys);
        mphfBuilder.numKeys(keys.size());
        mphfBuilder.transform(TransformationStrategies.prefixFreeUtf16());
    }

    @Override
    void construct(List<String> keys) {
        try {
            mphf = mphfBuilder.build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    long performQuery(String key) {
        return mphf.getLong(key);
    }
}

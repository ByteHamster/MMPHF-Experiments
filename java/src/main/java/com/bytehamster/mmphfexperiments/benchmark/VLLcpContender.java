package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.sux4j.mph.VLLcpMonotoneMinimalPerfectHashFunction;

import java.io.IOException;
import java.util.List;

public class VLLcpContender extends Contender {
    VLLcpMonotoneMinimalPerfectHashFunction<String> mphf;

    @Override
    String name() {
        return "VLLcpJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void construct(List<String> keys) {
        try {
            mphf = new VLLcpMonotoneMinimalPerfectHashFunction<>(keys, TransformationStrategies.prefixFreeUtf16());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    long performQuery(String key) {
        return mphf.getLong(key);
    }
}

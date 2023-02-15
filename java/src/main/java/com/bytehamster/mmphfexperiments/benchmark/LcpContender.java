package com.bytehamster.mmphfexperiments.benchmark;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.sux4j.mph.LcpMonotoneMinimalPerfectHashFunction;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class LcpContender extends Contender {
    LcpMonotoneMinimalPerfectHashFunction.Builder<String> mphfBuilder;
    LcpMonotoneMinimalPerfectHashFunction<String> mphf;

    @Override
    String name() {
        return "LcpJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void beforeConstruction(List<String> keys) {
        ((Logger) LoggerFactory.getLogger(LcpMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        mphfBuilder = new LcpMonotoneMinimalPerfectHashFunction.Builder<>();
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

package com.bytehamster.mmphfexperiments.benchmark;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.sux4j.mph.ZFastTrieDistributorMonotoneMinimalPerfectHashFunction;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ZFastTrieDistributorContender extends Contender {
    ZFastTrieDistributorMonotoneMinimalPerfectHashFunction.Builder<String> mphfBuilder;
    ZFastTrieDistributorMonotoneMinimalPerfectHashFunction<String> mphf;

    @Override
    String name() {
        return "ZFastTrieDistributorJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void beforeConstruction(List<String> keys) {
        ((Logger) LoggerFactory.getLogger(ZFastTrieDistributorMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        mphfBuilder = new ZFastTrieDistributorMonotoneMinimalPerfectHashFunction.Builder<>();
        mphfBuilder.keys(keys);
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

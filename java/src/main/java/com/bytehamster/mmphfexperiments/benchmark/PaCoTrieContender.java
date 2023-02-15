package com.bytehamster.mmphfexperiments.benchmark;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.sux4j.mph.PaCoTrieDistributorMonotoneMinimalPerfectHashFunction;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class PaCoTrieContender extends Contender {
    PaCoTrieDistributorMonotoneMinimalPerfectHashFunction<String> mphf;

    @Override
    String name() {
        return "PaCoTrieJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void beforeConstruction(List<String> keys) {
        super.beforeConstruction(keys);
        ((Logger) LoggerFactory.getLogger(PaCoTrieDistributorMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
    }

    @Override
    void construct(List<String> keys) {
        try {
            mphf = new PaCoTrieDistributorMonotoneMinimalPerfectHashFunction<>(keys, TransformationStrategies.prefixFreeUtf16());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    long performQuery(String key) {
        return mphf.getLong(key);
    }
}

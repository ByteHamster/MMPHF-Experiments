package com.bytehamster.mmphfexperiments.benchmark;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.sux4j.mph.HollowTrieMonotoneMinimalPerfectHashFunction;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HollowTrieContender extends Contender {
    HollowTrieMonotoneMinimalPerfectHashFunction<String> mphf;
    
    @Override
    String name() {
        return "HollowTrieJava";
    }

    @Override
    long sizeBits() {
        return mphf.numBits();
    }

    @Override
    void beforeConstruction(List<String> keys) {
        super.beforeConstruction(keys);
        ((Logger) LoggerFactory.getLogger(HollowTrieMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
    }

    @Override
    void construct(List<String> keys) {
        mphf = new HollowTrieMonotoneMinimalPerfectHashFunction<>(keys, TransformationStrategies.prefixFreeUtf16());
    }

    @Override
    long performQuery(String key) {
        return mphf.getLong(key);
    }
}

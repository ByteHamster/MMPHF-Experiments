package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.sux4j.mph.HollowTrieMonotoneMinimalPerfectHashFunction;

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
    void construct(List<String> keys) {
        mphf = new HollowTrieMonotoneMinimalPerfectHashFunction<>(keys, TransformationStrategies.prefixFreeUtf16());
    }

    @Override
    long performQuery(String key) {
        return mphf.getLong(key);
    }
}

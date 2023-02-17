package com.bytehamster.mmphfexperiments.benchmark;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import it.unimi.dsi.sux4j.mph.GOV3Function;
import it.unimi.dsi.sux4j.mph.GOVMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.HollowTrieDistributor;
import it.unimi.dsi.sux4j.mph.HollowTrieDistributorMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.HollowTrieMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.LcpMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.PaCoTrieDistributor;
import it.unimi.dsi.sux4j.mph.PaCoTrieDistributorMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.TwoStepsGOV3Function;
import it.unimi.dsi.sux4j.mph.TwoStepsLcpMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.VLLcpMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.VLPaCoTrieDistributor;
import it.unimi.dsi.sux4j.mph.VLPaCoTrieDistributorMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.ZFastTrieDistributor;
import it.unimi.dsi.sux4j.mph.ZFastTrieDistributorMonotoneMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.solve.Linear3SystemSolver;
import it.unimi.dsi.sux4j.mph.solve.Modulo2System;
import it.unimi.dsi.sux4j.mph.solve.Modulo3System;
import org.slf4j.LoggerFactory;

public class Sux4jSetup {
    public static void setup() {
        System.setProperty("it.unimi.dsi.sux4j.mph.threads", "1");
        ((Logger) LoggerFactory.getLogger(HollowTrieMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(HollowTrieDistributorMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(LcpMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(PaCoTrieDistributorMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(TwoStepsLcpMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(Linear3SystemSolver.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(Modulo2System.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(TwoStepsGOV3Function.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(GOV3Function.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(VLLcpMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(VLPaCoTrieDistributorMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(ZFastTrieDistributor.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(HollowTrieDistributor.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(PaCoTrieDistributor.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(GOVMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(Modulo3System.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(ZFastTrieDistributorMonotoneMinimalPerfectHashFunction.class)).setLevel(Level.OFF);
        ((Logger) LoggerFactory.getLogger(VLPaCoTrieDistributor.class)).setLevel(Level.OFF);
    }
}

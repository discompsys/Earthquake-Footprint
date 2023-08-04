/*
 * TestFGD1.java
 *
 * Created on 1 June 2023
 *
 * Version 1.0
 *
 * Copyright (c) Kieran Greer
 */

package eq_fp.test;


import eq_fp.algorithm.Analyse;
import eq_fp.algorithm.Simulate;
import org.jlog2.CustomLoggerFactory;
import org.jlog2.LoggerFactory;


/** Set of tests to evaluate earthquake events */
public class TestEQ {

    /** Train file path */
    protected String trainFile;

    /** Events file path */
    protected String eventsFile;

    /** Clusters file path */
    protected String clustersFile;

    /** Analysis file path */
    protected String analysisFile;

    /** Prediction file path */
    protected String predictFile;


    /**
     * Create a new instance of TestEQ.
     */
    public TestEQ() {
        try {
            LoggerFactory.setLoggerFactory(new CustomLoggerFactory());
        }
        catch (Exception ex) {
            ex.printStackTrace();;
        }
    }

    /**
     * Compare the patterns for similarity / difference.
     * @param args input arguments.
     */
    protected void analyseData(String[] args) throws Exception {

        Analyse analysis;                                  //to analyse the data

        try {
            analysis = new Analyse();
            analysis.analyse(args, eventsFile, clustersFile, analysisFile);
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Run predictions to try to guess the significant events.
     * @param args input arguments.
     */
    protected void runPredictions(String[] args) throws Exception {

        Simulate sim;                               //to predict events

        try {
            sim = new Simulate();
            sim.runPredictions(args, eventsFile, clustersFile, analysisFile, predictFile);
        }
        catch (Exception ex) {
            throw ex;
        }
    }
}

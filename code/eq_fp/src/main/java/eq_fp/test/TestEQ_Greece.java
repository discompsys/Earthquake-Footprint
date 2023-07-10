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


import eq_fp.model.ReadData;
import eq_fp.util.DataConst;
import eq_fp.util.EqConst;


/** Evaluate earthquake events on the Greece dataset */
public class TestEQ_Greece extends TestEQ {

    /**
     * Create a new instance of TestFGD1.
     * @param args input arguments.
     */
    public TestEQ_Greece(String[] args) {
        super();

        try {
            String timeUnit;                                    //time unit
            String[] testArgs;                                  //test arguments

            trainFile = (DataConst.DATAROOT + "greece_earthquake\\Earthquakes_Greece.txt");
            eventsFile = (DataConst.TESTROOT + "greece_events.txt");
            clustersFile = (DataConst.TESTROOT + "greece_clusters.txt");
            analysisFile = (DataConst.TESTROOT + "greece_analysis.txt");
            predictFile = (DataConst.TESTROOT + "greece_predict.txt");
            timeUnit =  EqConst.DAY;

            testArgs = new String[4];
            testArgs[0] = "14";
            testArgs[1] = "0.5";
            testArgs[2] = "T";
            testArgs[3] = timeUnit;

            ReadData.formatData(trainFile, eventsFile, clustersFile, testArgs);

            // analyse the data to generate the footprints
            //can change the number of comparisons by decreasing the magnitude
            // or the key values by increasing or decreasing the number of days before
            testArgs = new String[4];
            testArgs[0] = "12";
            testArgs[1] = "200";
            testArgs[2] = timeUnit;
            testArgs[3] = "5";

            analyseData(testArgs);

            //run predictions to try to guess significant events
            runPredictions(testArgs);

            System.out.println("Finished");
        }
        catch (Exception ex) {
            ex.printStackTrace();;
        }
    }


    /** Main method */
    public static void main(String[] args) {
        new TestEQ_Greece(args);
    }
}

/*
 * Analyse.java
 *
 * Created on 26 June 2023
 *
 * Version 1.0
 *
 * Copyright (c) Kieran Greer
 */

package eq_fp.algorithm;


import eq_fp.model.AnalysisModel;
import eq_fp.model.TargetEvent;
import eq_fp.model.ReadData;
import eq_fp.util.EqConst;
import org.licas.ai_solver.model.result.Cluster;

import java.io.FileOutputStream;
import java.util.*;


/** Analyse the data for significant patterns */
public class Analyse {

    /**
     * Create a new instance of Analyse.
     */
    public Analyse()
    {}

    /**
     * Compare the patterns for similarity / difference.
     * @param args input arguments.
     * @param eventsFile file with the events.
     * @param clustersFile file with the clusters.
     * @param analysisFile file to save the analysis to.
     * @throws java.lang.Exception any error.
     */
    public void analyse(String[] args, String eventsFile, String clustersFile,
                        String analysisFile) throws Exception {

        ArrayList<Cluster> clusters;                        //generated clusters
        AnalysisModel am;                                   //analysis model
        LinkedHashMap<String, ArrayList<String>> dataRows;  //all data rows

        try {
            dataRows = ReadData.readEvents(eventsFile);
            clusters = ReadData.readClusters(clustersFile);
            System.out.println("Data Read.");

            //analyse patterns related to the significant event
            am = compareEvents(args, dataRows, clusters);

            //print out the result
            printAnalysis(am);
            writeAnalysis(am, analysisFile);

            //do some stats on the result
            analysisStats(am);
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Compare the patterns for changes.
     * @param dataRows input events.
     * @param clusters global clusters.
     * @param args input arguments.
     * @return the generated analysis.
     */
    private AnalysisModel compareEvents(String[] args,
                                        LinkedHashMap<String, ArrayList<String>> dataRows,
                                        ArrayList<Cluster> clusters) {

        int i, j;
        int minMag;                                         //minimum magnitude to find
        int unitsBefore;                                    //number time units before
        long start, end;                                    //start and time range
        String unitSize;                                    //time unit size
        String ana1, ana2;                                  //analysis description
        AnalysisModel am;                                   //analysis model
        TargetEvent tg, tg2;                                //target event
        TargetScore ts;                                     //to evaluate the events
        Date date;                                          //date time

        //scan and retrieve all events with magnitude larger than X
        // retrieve the clusters for those
        // run the test below for each cluster and for all the other ones using that cluster's date
        // can produce result of number days in result / average gap size / average number of events per day ?

        //get list of events clustered with the global significant one
        minMag = Integer.parseInt(args[0]);
        unitsBefore = Integer.parseInt(args[1]);
        unitSize = args[2];
        am = new AnalysisModel(dataRows, clusters, minMag);
        ts = new TargetScore();

        //get result for the event with event dates and other significant ones for same dates and compare
        for (i = 0; i < am.targetEvents.size(); i++) {

            tg = am.targetEvents.get(i);

            //get lists of events for dates before significant date
            date = new Date(tg.date);
            end = date.getTime();
            start = (end - (EqConst.convertToMillisec(unitSize) * unitsBefore));

            ana1 = ts.targetScore(tg, start, end, dataRows);
            am.keyClusters.put(ana1, new ArrayList<>());

            for (j = 0; j < am.targetEvents.size(); j++) {

                if (i != j) {
                    tg2 = am.targetEvents.get(j);
                    ana2 = ts.targetScore(tg2, start, end, dataRows);
                    am.keyClusters.get(ana1).add(ana2);
                }
            }
        }

        return am;
    }

    /**
     * Generate some stats on the analysis.
     * @param analysis the analysis description.
     */
    private void analysisStats(AnalysisModel analysis) throws Exception {
        int count, num;                                     //count
        String[] keyLoc, keyLoc2;                           //key and location
        ArrayList<Integer> relCount;                        //related counts
        ArrayList<String> sigKeys;                          //significant event keys
        ArrayList<String> relKeys;                          //related keys
        ArrayList<String> events;                           //related events

        sigKeys = new ArrayList(analysis.keyClusters.keySet());

        //count percentage with valid key
        count = 0;
        num = 0;

        for (String key : sigKeys) {
            keyLoc = EqConst.fromCompoundKey(key);
            if (EqConst.hasFootprint(keyLoc[0])) count++;
            num++;
        }

        System.out.println("\nPercentage of sig keys with footprint: " + ((float)count / (float)num) * 100);

        relCount = new ArrayList<>();
        for (String key : sigKeys) {
            keyLoc = EqConst.fromCompoundKey(key);

            if (EqConst.hasFootprint(keyLoc[0])) {
                count = 1;
                relKeys = new ArrayList<>();
                relKeys.add(key);

                for (String key2 : sigKeys) {
                    keyLoc2 = EqConst.fromCompoundKey(key2);

                    if (!keyLoc2[0].equalsIgnoreCase(keyLoc[0])) {
                        events = analysis.keyClusters.get(key2);

                        for (String eventKey : events) {
                            keyLoc2 = EqConst.fromCompoundKey(eventKey);

                            if (keyLoc2[0].equalsIgnoreCase(keyLoc[0])) {
                                if (!keyLoc2[1].equalsIgnoreCase(keyLoc[1])) {
                                    relKeys.add(key2);
                                    count++;
                                }
                            }
                        }
                    }
                }

                relCount.add(count);
            }
        }

        count = 0;
        for (Integer rCount : relCount) {
            count += rCount;
        }

        System.out.println("Average duplication of significant keys count: " + ((float)count / relCount.size()));
    }

    /**
     * Print out the generated analysis.
     * @param analysis the generated analysis.
     */
    private void printAnalysis(AnalysisModel analysis) {

        ArrayList<String> keyList;                          //key list
        ArrayList<String> valueList;                        //value list

        keyList = new ArrayList(analysis.keyClusters.keySet());

        System.out.println("\nKey events:");
        for (String key : keyList) {
            System.out.println(key);
        }

        System.out.println("\nEvent comparisons:");
        for (String key : keyList) {
            System.out.println("\n" + key);

            valueList = analysis.keyClusters.get(key);
            for (String key2 : valueList) {
                System.out.println("   " + key2);
            }
        }
    }

    /**
     * Write the generated analysis to a file.
     * @param analysis the generated analysis.
     * @throws java.lang.Exception any error.
     */
    private void writeAnalysis(AnalysisModel analysis, String analysisFile) throws Exception {

        String descr;                                       //description
        ArrayList<String> keyList;                          //key list
        ArrayList<String> valueList;                        //value list
        FileOutputStream writer;                            //output file

        writer = new FileOutputStream(analysisFile);
        keyList = new ArrayList(analysis.keyClusters.keySet());

        descr = (EqConst.TARGETEVENTS + "\n");
        writer.write(descr.getBytes());

        for (TargetEvent targetEvent : analysis.targetEvents) {
            descr = (targetEvent.event + EqConst.ADDRSEP + targetEvent.date + "\n");
            writer.write(descr.getBytes());
        }

        descr = ("\n" + EqConst.KEYEVENTS + "\n");
        writer.write(descr.getBytes());

        for (String key : analysis.keyClusters.keySet()) {
            descr = (key + "\n");
            writer.write(descr.getBytes());
        }

        descr = ("\n" + EqConst.KEYCLUSTERS);
        writer.write(descr.getBytes());

        for (String key : keyList) {
            descr = ("\n" + key + "\n");
            writer.write(descr.getBytes());

            valueList = analysis.keyClusters.get(key);
            for (String key2 : valueList) {
                descr = ("  " + key2 + "\n");
                writer.write(descr.getBytes());
            }
        }

        writer.close();
    }
}

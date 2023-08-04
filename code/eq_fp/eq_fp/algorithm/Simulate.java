/*
 * Simulate.java
 *
 * Created on 4 July 2023
 *
 * Version 1.0
 *
 * Copyright (c) Kieran Greer
 */

package eq_fp.algorithm;


import eq_fp.model.TargetEvent;
import eq_fp.model.AnalysisModel;
import eq_fp.model.ReadData;
import eq_fp.util.EqConst;
import org.licas.ai_solver.model.result.Cluster;
import org.ai_heuristic.util.SymbolHandler;
import org.jlog2.util.FileLoader;
import org.jlog2.util.StringHandler;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;


/** This class tries to predict what significant event will occur at what time */
public class Simulate {

    /**
     * Location of significant events with dates.
     * Key is the event date and value is the related location.
     */
    protected HashMap<String, String> dateLoc;

    /**
     * Analysis key of significant event with locations.
     * Key is the location and value is the related analysis description.
     */
    protected HashMap<String, String> locAnaKey;


    /** Crate a new instance of Simulate */
    public Simulate()
    {
        dateLoc = new HashMap<>();
        locAnaKey = new HashMap<>();
    }

    public void runPredictions(String[] args, String eventsFile, String clustersFile, String analysisFile,
                               String predictFile) {

        int i;
        int minMag;                                         //minimum magnitude to find
        int unitsBefore;                                    //number time units before
        long start, end;                                    //start and time range
        long firstTime;                                     //time for first row
        float margin;                                       //percent error margin either way
        String unitSize;                                    //time unit size
        String ana1, ana2;                                  //analysis description
        String targetKey, compareKey;                       //prediction keys
        String dateKey;                                     //date key
        String locKey;                                      //location key
        Object[] keyParts;                                  //key parts
        ArrayList<String> dateKeys;                         //date keys
        LinkedHashMap<String, ArrayList<String>> dataRows;  //selected data rows
        HashMap<String, ArrayList<String>> matchEvents;     //list of matching sequences
        ArrayList<Cluster> clusters;                        //generated clusters
        AnalysisModel am;                                   //analysis model
        TargetScore ts;                                     //to evaluate the events
        Date date;                                          //date time

        try {
            minMag = Integer.parseInt(args[0]);
            unitsBefore = Integer.parseInt(args[1]);
            unitSize = args[2];
            margin = Float.parseFloat(args[3]);
            firstTime = -1;
            matchEvents = new HashMap<>();

            //read the data into structures
            dataRows = ReadData.readEvents(eventsFile);
            clusters = ReadData.readClusters(clustersFile);
            am = new AnalysisModel(dataRows, clusters, minMag);
            readAnalysisFile(analysisFile);
            ts = new TargetScore();

            //Tadd each line in turn and set end date to last line
            // start date is then determined by the time window
            // for each target cluster evaluate th selected events dor the cluster
            // if returned key is xx% of actual key, then note the date for the similarity

            //process cumulatively through the event rows and try to compare with significant events
            dateKeys = new ArrayList(dataRows.keySet());
            for (i = 0; i < dateKeys.size(); i++) {
                dateKey = dateKeys.get(i);
                date = new Date(dateKey);

                end = date.getTime();
                if (i == 0) {
                    firstTime = end;
                }

                start = (end - (EqConst.convertToMillisec(unitSize) * unitsBefore));
                if (start < firstTime) start = firstTime;

                for (TargetEvent targetEvent : am.targetEvents) {
                    dateKey = targetEvent.date;
                    locKey = dateLoc.get(dateKey);
                    ana1 = locAnaKey.get(locKey);

                    targetKey = EqConst.toCompoundKey(targetEvent.event, targetEvent.date);
                    targetKey = EqConst.toCompoundKey(ana1, targetKey);
                    keyParts = EqConst.fromEventKey(ana1);

                    if (((int)keyParts[0]) > 0) {
                        ana2 = EqConst.fromCompoundKey(ts.targetScore(targetEvent, start, end, dataRows))[0];

                        if (analysisMatch(ana1, ana2, margin)) {
                            compareKey = EqConst.toCompoundKey(ana2, date.toString());

                            if (!matchEvents.containsKey(targetKey)) {
                                matchEvents.put(targetKey, new ArrayList<>());
                            }

                            matchEvents.get(targetKey).add(compareKey);
                        }
                    }
                }
            }

            //output the result
            printPredictions(matchEvents);
            writePredictions(matchEvents, predictFile);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean analysisMatch(String an1, String an2, float margin) {
        float fVal1, fVal2;                     //float values
        float error;                            //error size
        float diff;                             //difference in the values
        Object[] el1, el2;                      //event lists

        //all parts must be inside the margin of error individually for a match
        el1 = EqConst.fromEventKey(an1);
        el2 = EqConst.fromEventKey(an2);

        fVal1 = Float.parseFloat(String.valueOf(el1[0]));
        fVal2 = Float.parseFloat(String.valueOf(el2[0]));
        error = (fVal1 / 100.0f * margin);
        diff = Math.abs(fVal1 - fVal2);

        if (diff <= error) {
            fVal1 = Float.parseFloat(String.valueOf(el1[1]));
            fVal2 = Float.parseFloat(String.valueOf(el2[1]));
            error = (fVal1 / 100.0f * margin);
            diff = Math.abs(fVal1 - fVal2);

            if (diff <= error) {
                fVal1 = Float.parseFloat(String.valueOf(el1[2]));
                fVal2 = Float.parseFloat(String.valueOf(el2[2]));
                error = (fVal1 / 100.0f * margin);
                diff = Math.abs(fVal1 - fVal2);

                if (diff <= error) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Read the analysis file and create structure relating date, location and key score values.
     * @param analysisFile the analysis file path.
     * @throws Exception any error.
     */
    private void readAnalysisFile(String analysisFile) throws Exception {
        int i;
        String input;                                   //input text
        String nextLine;                                //next line
        String[] compKey;                               //compound key
        ArrayList<String> lineList;                     //all lines

        input = FileLoader.getInputStream(analysisFile);
        lineList = StringHandler.tokenize(input, SymbolHandler.NL, false);

        for (i = 0; i < lineList.size(); i++) {
            nextLine = lineList.get(i);

            if (nextLine.contains(EqConst.TARGETEVENTS)) {
                i++;
                for (; i < lineList.size(); i++) {
                    nextLine = lineList.get(i);

                    if (!nextLine.trim().isEmpty()) {
                        compKey = EqConst.fromCompoundKey(nextLine.trim());
                        dateLoc.put(compKey[1], compKey[0]);
                    }
                    else {
                        break;
                    }
                }
            }
            else if (nextLine.contains(EqConst.KEYEVENTS)) {
                i++;
                for (; i < lineList.size(); i++) {
                    nextLine = lineList.get(i);

                    if (!nextLine.trim().isEmpty()) {
                        compKey = EqConst.fromCompoundKey(nextLine.trim());
                        locAnaKey.put(compKey[1], compKey[0]);
                    }
                    else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Print out the generated analysis.
     * @param matchEvents the predicted events that match the significant ones.
     */
    private void printPredictions(HashMap<String, ArrayList<String>> matchEvents) {

        for (String matchKey : matchEvents.keySet()) {
            System.out.println("\nMatch events: " + matchKey);

            for (String matchEvent : matchEvents.get(matchKey)) {
                System.out.println("   " + matchEvent);
            }
        }
    }

    /**
     * Write the generated analysis to a file.
     * @param matchEvents the predicted events that match the significant ones.
     * @param predictFile path of file to write to.
     * @throws java.lang.Exception any error.
     */
    private void writePredictions(HashMap<String, ArrayList<String>> matchEvents, String predictFile) throws Exception {

        String descr;                                       //description
        FileOutputStream writer;                            //output file

        writer = new FileOutputStream(predictFile);

        for (String matchKey : matchEvents.keySet()) {
            descr = ("\nMatch events: " + matchKey + "\n");
            writer.write(descr.getBytes());

            for (String matchEvent : matchEvents.get(matchKey)) {
                descr = ("   " + matchEvent + "\n");
                writer.write(descr.getBytes());
            }
        }

        writer.close();
    }
}

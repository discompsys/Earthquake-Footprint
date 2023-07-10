/*
 * AnalysisModel.java
 *
 * Created on 26 June 2023
 *
 * Version 1.0
 *
 * Copyright (c) Kieran Greer
 */

package eq_fp.model;


import eq_fp.util.DataConst;
import org.licas.ai_solver.model.result.Cluster;
import org.jlog2.util.StringHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


/** Stores the analysis model */
public class AnalysisModel {

    /** List of target clusters */
    public ArrayList<TargetEvent> targetEvents;

    /** The analysis description */
    public HashMap<String, ArrayList<String>> keyClusters;


    /** Create a new instance of AnalysisModel */
    public AnalysisModel(LinkedHashMap<String, ArrayList<String>> dataRows,
                         ArrayList<Cluster> clusters, int minMag) {
        targetEvents = new ArrayList<>();
        keyClusters = new HashMap<>();
        clustersToTargets(dataRows, clusters, minMag);
    }

    /**
     * Generate the target event clusters from the input data.
     * @param dataRows list of event data rows.
     * @param clusters list of frequency gridt clusters.
     * @param minMag threshold for a significant event.
     */
    private void clustersToTargets(LinkedHashMap<String, ArrayList<String>> dataRows,
                                   ArrayList<Cluster> clusters, int minMag) {
        int i;
        String nextKey;
        ArrayList<String> added;                            //events added
        ArrayList<TargetEvent> allEvents;                   //list of all target clusters
        Cluster cluster;
        TargetEvent tg, tg2;

        for (i = 0; i < clusters.size(); i++) {
            cluster = clusters.get(i);

            for (Object obj : cluster.list) {
                nextKey = (String)obj;
                if (getMagnitude(nextKey) >= minMag) {
                    tg = new TargetEvent();
                    tg.event = nextKey;
                    tg.cluster = cluster;
                    targetEvents.add(tg);
                    break;
                }
            }
        }

        //can ensure only 1 copy, but leads to very few footprints
        //added = new ArrayList<>();

        //significant event can occur more than once, so try to copy all occurrences
        allEvents = new ArrayList<>();

        for (i = 0; i < targetEvents.size(); i++) {
            tg = targetEvents.get(i);

            if (tg.cluster != null) {
                for (String datestr : dataRows.keySet()) {
                    //if (added.contains(tg.event)) break;

                    if (dataRows.get(datestr).contains(tg.event)) {
                        tg2 = (TargetEvent) tg.clone();
                        tg2.date = datestr;
                        tg2.dataRow = dataRows.get(datestr);
                        allEvents.add(tg2);

                        //added.add(tg2.event);
                    }
                }
            }
        }

        targetEvents = allEvents;
    }

    /**
     * Get the magnitude part of the event key.
     * @param eventKey whole event description.
     * @return magnitude only.
     */
    private int getMagnitude(String eventKey) {
        ArrayList<String> tokens;               //tokens

        tokens = StringHandler.tokenize(eventKey, DataConst.KEYSEP, false);
        return Integer.parseInt(tokens.get(2));
    }
}

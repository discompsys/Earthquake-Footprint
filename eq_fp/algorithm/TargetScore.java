/*
 * TargetScore.java
 *
 * Created on 26 June 2023
 *
 * Version 1.0
 *
 * Copyright (c) Kieran Greer
 */

package eq_fp.algorithm;


import eq_fp.model.TargetEvent;
import eq_fp.util.DataConst;
import eq_fp.util.EqConst;
import org.licas.ai_solver.model.result.Cluster;
import org.jlog2.util.StringHandler;

import java.util.*;


/** Generates a score for data events in a time window, against the target cluster */
public class TargetScore {

    /**
     * Create a new instance of TargetScore.
     */
    public TargetScore()
    {}

    /**
     * Compare the patterns for changes.
     * @param targetEvent the event set to analyse.
     * @param start start time in milliseconds for the date range.
     * @param end end time in milliseconds for the date range.
     * @param dataRows input events.
     * @return comparison description - key description.
     */
    public String targetScore(TargetEvent targetEvent, long start, long end,
                              HashMap<String, ArrayList<String>> dataRows) {

        int i;
        int count;                                                  //count of something
        double cohCount;                                            //cohesion count
        double diff;                                                //difference
        String nextKey;                                             //next key
        ArrayList<Long> tsOrdered;                                  //ordered timestamps
        ArrayList<Double> tsDiff;                                   //timestamp differences
        ArrayList<Double> tsCoh;                                    //timestamp cohesions
        ArrayList<String> eventKeys;                                //event date keys
        ArrayList<String> sigEvents;                                //sig event coords only
        ArrayList<String> sigEventsCoords;                          //sig event coords only
        ArrayList<String> eventList, eventList2;                    //list of events
        HashMap<String, Integer> eventCount;                        //count of matching events key magnitude
        HashMap<String, Integer> eventCountCoord;                   //count of matching events coords only
        HashMap<String, ArrayList<Double>> eventMag;                //list of magnitudes for each event key
        LinkedHashMap<Long, String> dateHash;                       //dates with timestamps
        LinkedHashMap<String, ArrayList<String>> events;            //all related events key magnitude
        LinkedHashMap<String, ArrayList<String>> eventCoords;       //all related events coords only
        Date date;                                                  //time date
        Cluster targetCluster;                                      //the target cluster

        try {
            //get list of events clustered with the global significant one
            targetCluster = targetEvent.cluster;
            sigEvents = targetEvent.dataRow;
            sigEventsCoords = eventCoords(targetEvent.dataRow);
            events = new LinkedHashMap<>();
            eventCoords = new LinkedHashMap<>();

            //get date and input events for significant event and other related events
            //so all events that might be related to the significant one
            if (targetCluster != null) {
                for (String datestr : dataRows.keySet()) {
                    for (Object obj : targetCluster.list) {
                        nextKey = (String) obj;
                        if (dataRows.get(datestr).contains(nextKey)) {
                            events.put(datestr, dataRows.get(datestr));
                            eventCoords.put(datestr, eventCoords(dataRows.get(datestr)));
                            break;
                        }
                    }
                }
            }

            //remove the target event first if it is present in the rows
            events.remove(targetEvent.date);
            eventCoords.remove(targetEvent.date);

            //get lists of events for dates before significant date
            eventKeys = new ArrayList(events.keySet());
            for (String dateKey : eventKeys) {
                date = new Date(dateKey);

                if ((date.getTime() < start) || (date.getTime() > end)) {
                    events.remove(dateKey);
                    eventCoords.remove(dateKey);
                }
            }

            //get count of number of occurrences of key
            // for the global cluster keys every day before for x days
            eventCount = new HashMap<>();
            eventCountCoord = new HashMap<>();
            eventMag = new HashMap<>();
            dateHash = new LinkedHashMap<>();

            eventKeys = new ArrayList(events.keySet());
            for (String dateKey : eventKeys) {
                eventList = events.get(dateKey);
                eventList2 = eventCoords.get(dateKey);
                dateHash.put((new Date(dateKey)).getTime(), dateKey);

                count = 0;
                for (String key : eventList) {
                    if (sigEvents.contains(key)) count++;
                }
                eventCount.put(dateKey, count);

                count = 0;
                for (String key : eventList2) {
                    if (sigEventsCoords.contains(key)) count++;
                }
                eventCountCoord.put(dateKey, count);

                if (!eventMag.containsKey(dateKey)) {
                    eventMag.put(dateKey, new ArrayList<>());
                }
                for (String key : eventList) {
                    eventMag.get(dateKey).add(Double.parseDouble(DataConst.fromDataKey(key).get(2)));
                }
            }

            tsOrdered = new ArrayList(dateHash.keySet());
            Collections.sort(tsOrdered);
            Collections.reverse(tsOrdered);

            //test if the time between significant days changes
            //reverse again so can start from earliest date, not closest one
            Collections.reverse(tsOrdered);
            tsDiff = new ArrayList<>();
            tsCoh = new ArrayList<>();

            cohCount = 0;
            for (i = 1; i < tsOrdered.size(); i++) {
                diff = (tsOrdered.get(i) - tsOrdered.get(i - 1));
                diff /= EqConst.convertFromMillisec(EqConst.DAY);
                tsDiff.add(diff);

                if (diff > 1) {
                    if (cohCount > 0) {
                        tsCoh.add(cohCount);
                        cohCount = 0;
                    }
                }
                else {
                    cohCount++;
                }
            }
            if (cohCount > 0) {
                tsCoh.add(cohCount);
            }

            //for testing
            //System.out.println("Day gaps: " + tsDiff);
            //System.out.println("Day cohesions: " + tsCoh);

            //generate a result description
            return EqConst.toCompoundKey(tsDescription(tsDiff, tsCoh, eventCount, eventMag), targetEvent.event);
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Generate a description of the timestamps representing event days.
     * @param tsDiff list of day gaps.
     * @param tsCoh list of continuous days.
     * @param eventCount number of relevant events each day.
     * @param eventMag list of magnitudes for each event key.
     * @return analysis description of the list.
     */
    private String tsDescription(ArrayList<Double> tsDiff, ArrayList<Double> tsCoh,
                                 HashMap<String, Integer> eventCount,
                                 HashMap<String, ArrayList<Double>> eventMag) {
        int avEvCount;                          //average event count
        float magValue;                         //magnitude value
        double avGap;                           //average gap size in days
        double avCoh;                           //average cohesion size in days
        double avMag;                           //average magnitude
        double coh;                             //possible cohesion value
        ArrayList<Double> magAverages;          //average magnitude each day

        avEvCount = 0;
        if (!eventCount.isEmpty()) {
            for (String key : eventCount.keySet()) {
                avEvCount += eventCount.get(key);
            }
            avEvCount /= eventCount.size();
        }

        //day gaps and cohesion were helpful
        //magnitude was not helpful
        magAverages = new ArrayList<>();
        for (String key : eventMag.keySet()) {
            coh = EqConst.cohesion(eventMag.get(key), eventMag.get(key).size());
            magAverages.add(coh / eventMag.get(key).size());
        }

        avGap = 0;
        avMag = 0;
        coh = 0;
        if (!tsDiff.isEmpty()) {
            //not very good - less accurate
            //coh = EqConst.cohesion(magAverages, magAverages.size());

            for (double tsValue : tsDiff) {
                avGap += tsValue;
            }
            avGap /= tsDiff.size();
        }

        avCoh = 0;
        if (!tsCoh.isEmpty()) {
            for (double tsValue : tsCoh) {
                avCoh += tsValue;
            }
            avCoh /= tsCoh.size();
        }

        //return EqConst.toEventKey(tsDiff.size(), avGap, avEvCount);
        return EqConst.toEventKey(tsDiff.size(), avGap, avEvCount, avCoh);
    }

    /**
     * Parse the full event string to return the coords or location part only.
     * @param eventList list of events with full keys for each entry.
     * @return same list with only coords for the keys.
     */
    private ArrayList<String> eventCoords(ArrayList<String> eventList) {
        String valueStr;
        ArrayList<String> tokens;
        ArrayList<String> eventList2;

        eventList2 = new ArrayList<>();

        for (String eventStr : eventList) {
            tokens = StringHandler.tokenize(eventStr, DataConst.KEYSEP, false);
            valueStr = (tokens.get(0) + DataConst.KEYSEP + tokens.get(1));
            if (!eventList2.contains(valueStr)) eventList2.add(valueStr);
        }

        return eventList2;
    }
}

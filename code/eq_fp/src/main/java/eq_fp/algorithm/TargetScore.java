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
        double diff;                                                //difference
        String nextKey;                                             //next key
        ArrayList<Long> tsOrdered;                                  //ordered timestamps
        ArrayList<Double> tsDiff;                                   //timestamp differences
        ArrayList<String> eventKeys;                                //event date keys
        ArrayList<String> sigEvents;                                //sig event coords only
        ArrayList<String> sigEventsCoords;                          //sig event coords only
        ArrayList<String> eventList, eventList2;                    //list of events
        HashMap<String, Integer> eventCount;                        //count of matching events key magnitude
        HashMap<String, Integer> eventCountCoord;                   //count of matching events coords only
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
            }

            tsOrdered = new ArrayList(dateHash.keySet());
            Collections.sort(tsOrdered);
            Collections.reverse(tsOrdered);

            //test if the time between significant days changes
            //reverse again so can start from earliest date, not closest one
            Collections.reverse(tsOrdered);
            tsDiff = new ArrayList<>();

            for (i = 1; i < tsOrdered.size(); i++) {
                diff = (tsOrdered.get(i) - tsOrdered.get(i - 1));
                diff /= EqConst.convertFromMillisec(EqConst.DAY);
                tsDiff.add(diff);
            }

            //for testing
            //System.out.println("Day gaps: " + tsDiff);

            //generate a result description
            return EqConst.toCompoundKey(tsDescription(tsDiff, eventCount), targetEvent.event);
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Generate a description of the timestamps representing event days.
     * @param tsDiff list of day gaps.
     * @param eventCount number of relevant events each day.
     * @return analysis description of the list.
     */
    private String tsDescription(ArrayList<Double> tsDiff, HashMap<String, Integer> eventCount) {
        int avEvCount;                          //average event count
        double avValue;                         //average gap size in days

        avEvCount = 0;
        if (!eventCount.isEmpty()) {
            for (String key : eventCount.keySet()) {
                avEvCount += eventCount.get(key);
            }
            avEvCount /= eventCount.size();
        }

        avValue = 0;
        if (!tsDiff.isEmpty()) {
            for (double tsValue : tsDiff) {
                avValue += tsValue;
            }
            avValue /= tsDiff.size();
        }

        return EqConst.toEventKey(tsDiff.size(), avValue, avEvCount);
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

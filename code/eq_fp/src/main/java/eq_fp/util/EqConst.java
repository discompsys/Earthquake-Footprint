/*
 * EqConst.java
 *
 * Created on 6 May 2023
 *
 * Version 1.0
 *
 * Copyright (c) Kieran Greer
 */

package eq_fp.util;


import org.jlog2.util.StringHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


/** Some constant values */
public class EqConst {

    /** Analysis description */
    public static final String TARGETEVENTS = "Target Events";
    public static final String KEYEVENTS = "Key Events";
    public static final String KEYCLUSTERS = "Key Clusters";

    /** Defines compound key separator tag */
    public static final String COMPSEP = "/";

    /** Defines compound key address separator tag */
    public static final String ADDRSEP = " @ ";


    public static final String SECOND = "Second";
    public static final String MINUTE = "Minute";
    public static final String HOUR = "Hour";
    public static final String DAY = "Day";
    public static final String WEEK = "Week";
    public static final String MONTH = "Month";
    public static final String YEAR = "Year";


    /**
     * Return true if the event key can represent a footprint, instead of immediate or 0.
     * @param eventKey full descriptive key.
     * @return true if it can represent a footprint.
     */
    public static boolean hasFootprint(String eventKey) {
        Object[] eventParts;                    //the event parts

        eventParts = fromEventKey(eventKey);
        return (((int)eventParts[0]) > 0);
    }

    /**
     * Convert the key parts into a single compound key.
     * @param key1 first key.
     * @param key2 second key.
     * @return single compound key.
     */
    public static String toCompoundKey(String key1, String key2) {
        return (key1 + EqConst.ADDRSEP + key2);
    }

    /**
     * Convert the single key into it parts.
     * @param compKey full compound key.
     * @return separate key parts.
     */
    public static String[] fromCompoundKey(String compKey) {
        String[] keyParts;                      //the key parts
        ArrayList<String> tokens;               //tokens

        tokens = StringHandler.tokenize(compKey, ADDRSEP, false);

        keyParts = new String[2];
        keyParts[0] = tokens.get(0);
        if (tokens.size() > 1) {
            keyParts[1] = tokens.get(1);
        }
        else {
            keyParts[1] = null;
        }

        return keyParts;
    }

    /**
     * Convert the event values into a single descriptive key.
     * @param numGaps number of time unit gaps before the event.
     * @param avGap average gap size in time units.
     * @param numEvents number of events in related sequences.
     * @return descriptive key.
     */
    public static String toEventKey(int numGaps, double avGap, int numEvents) {
        String eventKey;                            //event key
        DecimalFormat dc;                       //decimal format

        dc = new DecimalFormat("0.0");

        eventKey = (String.valueOf(numGaps) + EqConst.COMPSEP);
        eventKey += (dc.format(avGap) + EqConst.COMPSEP);
        eventKey += (String.valueOf(numEvents));

        return eventKey;
    }

    /**
     * Convert the descriptive event key back into the component parts.
     * @param eventKey full descriptive key.
     * @return list of component parts that make up the key, in the correct type.
     */
    public static Object[] fromEventKey(String eventKey) {
        Object[] eventParts;                    //the event parts
        ArrayList<String> tokens;               //tokens

        tokens = StringHandler.tokenize(eventKey, COMPSEP, false);

        eventParts = new Object[3];
        eventParts[0] = Integer.parseInt(tokens.get(0));
        eventParts[1] = Double.parseDouble(tokens.get(1));
        if (tokens.size() > 2) {
            eventParts[2] = Integer.parseInt(tokens.get(2));
        }
        else {
            eventParts[2] = null;
        }

        return eventParts;
    }

    /**
     * Return an value that converts the specified time unit from milliseconds.
     * @param timeUnit time unit description - minutes, hours, days, etc.
     * @return a value to convert from milliseconds to the time unit.
     */
    public static long convertFromMillisec(String timeUnit) {
        long interval;               //time interval

        if (timeUnit.equals(SECOND)) interval = 1;
        else if (timeUnit.equals(MINUTE)) interval = 60;
        else if (timeUnit.equals(HOUR)) interval = 60 * 60;
        else if (timeUnit.equals(DAY)) interval = (60 * 60 * 24);
        else if (timeUnit.equals(WEEK)) interval = (60 * 60 * 24 * 7);
        else interval = (60 * 60 * 24 * 365);
        return (interval * 1000);
    }

    /**
     * Return an aggregation value that converts the specified time unit
     * to milliseconds.
     * @param timeUnit time unit description - minutes, hours, days, etc.
     * @return a value to aggregate rows from milliseconds into that unit.
     */
    public static long convertToMillisec(String timeUnit) {
        if (timeUnit.equals(MINUTE)) return TimeUnit.MINUTES.toMillis(1);
        else if (timeUnit.equals(HOUR)) return TimeUnit.HOURS.toMillis(1);
        else if (timeUnit.equals(DAY)) return TimeUnit.DAYS.toMillis(1);
        else if (timeUnit.equals(WEEK)) return TimeUnit.DAYS.toMillis(7);
        else return TimeUnit.DAYS.toMillis(365);
    }

    /**
     * Convert the calendar date into a value for time unit in the day.
     * @param calendar the full calendar date.
     * @return a relative time unit in the day.
     */
    public static int timeToDayPart(Calendar calendar, String timeUnit) {
        if (timeUnit.equalsIgnoreCase(YEAR)) {
            return calendar.get(Calendar.YEAR);
        }
        else if (timeUnit.equalsIgnoreCase(MONTH)) {
            //gregorian starts at 0
            return (calendar.get(Calendar.MONTH) + 1);
        }
        else if (timeUnit.equalsIgnoreCase(DAY)) {
            return calendar.get(Calendar.DAY_OF_MONTH);
        }
        else if (timeUnit.equalsIgnoreCase(HOUR)) {
            return calendar.get(Calendar.HOUR_OF_DAY);
        }
        else {
            return calendar.get(Calendar.MINUTE);
        }
    }
}

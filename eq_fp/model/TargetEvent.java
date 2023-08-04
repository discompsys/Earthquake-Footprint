/*
 * TargetEvent.java
 *
 * Created on 1 June 2023
 *
 * Version 1.0
 *
 * Copyright (c) Kieran Greer
 */

package eq_fp.model;


import org.licas.ai_solver.model.result.Cluster;
import java.util.ArrayList;


/** A significant event with details */
public class TargetEvent {

    /** The target event */
    public String event;

    /** Date for the event - relates to the data row */
    public String date;

    /** Data row with the event */
    public ArrayList<String> dataRow;

    /** ML cluster with the event - other related events */
    public Cluster cluster;


    /** Create a new instance of TargetEvent */
    public TargetEvent() {
        event = null;
        date = null;
        dataRow = null;
        cluster = null;
    }

    /**
     * Create and return a clone of this object.
     * @return a clone of this object.
     */
    public Object clone() {
        TargetEvent cloneEvent;         //clone event

        cloneEvent = new TargetEvent();
        cloneEvent.event = event;
        cloneEvent.date = date;
        cloneEvent.dataRow = dataRow;
        cloneEvent.cluster = cluster;

        return cloneEvent;
    }
}

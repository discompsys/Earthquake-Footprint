/*
 * ReadData.java
 *
 * Created on 1 June 2023
 *
 * Version 1.0
 *
 * Copyright (c) Kieran Greer
 */


package eq_fp.model;


import eq_fp.util.DataConst;
import eq_fp.util.EqConst;
import org.licas.ai_solver.central.grid.FrequencyGrid;
import org.licas.ai_solver.model.result.Cluster;
import org.licas.ai_solver.spec.test.TestSpec;
import org.licas.ai_solver.util.SolverConst;
import org.licas.util.TypeConst;
import org.jlog2.util.FileLoader;
import org.jlog2.util.StringHandler;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;


/** Read the raw data and create the frequency grid data to be analysed */
public class ReadData {

    /**
     * Read the raw dataset and re-format it into the files used for the analysis.
     * @param trainFile the train dataset.
     * @param eventsFile file to write the re-formatted data to.
     * @param clustersFile file to write the clusters to.
     * @param args input variables.
     */
    public static void formatData(String trainFile, String eventsFile, String clustersFile,
                                    String[] args) throws Exception {
        int i, j;
        int index;                                          //index
        int numBands;                                       //number of bands
        double bandSize;                                    //size of a band
        double value;                                       //value
        double lat, lng;                                    //latitude and longitude
        String timeUnit;                                    //time unit
        String timeSep;                                     //time separator
        String clusterStr;                                  //cluster string
        String gridKey, nextKey;                            //grid key
        String dateStr, dateStr2;                           //dates as string
        String formatDate;                                  //to reformat the date
        DecimalFormat df;                                   //decimal format
        ArrayList<String> categories;                       //list of categories
        ArrayList<String> fgRow;                            //grid row
        ArrayList<ArrayList<String>> fgDataset;             //grid dataset
        ArrayList dataRow;                                  //data row
        ArrayList<ArrayList> dataset;                       //the dataset
        TestSpec testSpec;                                  //test spec
        FrequencyGrid fgd;                                  //discrete frequency grid
        ArrayList clusters;                                 //generated clusters
        DatasetManager dataManager;                         //dataset manager
        FileOutputStream writer;                            //output file

        try {
            dataManager = new DatasetManager(trainFile);
            dataManager.createDataset();
            dataset = dataManager.getInputDatasets();

            testSpec = new TestSpec(null);
            testSpec.getTestScript().tokenizer = (",");
            testSpec.getTestScript().metricType = SolverConst.FREQGRIDV1;
            df = new DecimalFormat("###");
            numBands = Integer.parseInt(args[0]);
            bandSize = Double.parseDouble(args[1]);
            timeSep = args[2];
            timeUnit = args[3];

            //measure number of entries per day
            categories = new ArrayList<>();
            fgDataset = new ArrayList<>();
            fgRow = null;
            dateStr2 = "";
            writer = new FileOutputStream(eventsFile);

            for (i = 0; i < dataset.size(); i++) {
                dataRow = dataset.get(i);

                dateStr = (String) dataRow.get(0);
                lat = (double) dataRow.get(1);
                lng = (double) dataRow.get(2);
                value = (double) dataRow.get(3);

                //format the date to the required granularity
                index = dateStr.indexOf(timeSep);
                formatDate = dateStr.substring(0, index);

                //some dates may use hypens
                formatDate = formatDate.replace("-", "/");

                //move to time part - keep hours only
                dateStr = dateStr.substring(index + 1);

                //time is always colons
                index = dateStr.indexOf(":");
                if (timeUnit.equals(EqConst.HOUR)) {
                    formatDate += (" " + dateStr.substring(0, index) + ":00:00");
                }
                else {
                    formatDate += (" 00:00:00");
                }
                dateStr = formatDate;

                //create the key to indicate the event
                gridKey = (df.format(lat) + DataConst.KEYSEP + df.format(lng));
                for (j = 1; j <= numBands; j++) {
                    nextKey = (gridKey + DataConst.KEYSEP + j);
                    if (!categories.contains(nextKey)) {
                        categories.add(nextKey);
                    }
                }

                nextKey = (gridKey + DataConst.KEYSEP + (int) (value / bandSize));
                if (dateStr.equalsIgnoreCase(dateStr2)) {
                    fgRow.add(nextKey);
                } else {
                    if (fgRow != null) {
                        writer.write((dateStr + "  " + String.valueOf(fgRow) + "\n").getBytes());
                    }
                    fgRow = new ArrayList<>();
                    fgDataset.add(fgRow);
                    fgRow.add(nextKey);
                }

                dateStr2 = dateStr;
            }

            writer.close();
            writer = new FileOutputStream(clustersFile);

            fgd = new FrequencyGrid(testSpec);
            fgd.setGridEvents(fgDataset);
            fgd.solve(fgd.getMetric(TypeConst.STRING));

            clusters = fgd.getClusters();
            System.out.println("Clusters:");
            for (i = 0; i < clusters.size(); i++) {
                clusterStr = String.valueOf(clusters.get(i));
                clusterStr = clusterStr.substring(1, clusterStr.length() - 1);
                writer.write(("Cluster " + (i + 1) + "\n" + clusterStr + "\n").getBytes());

                System.out.println(String.valueOf(clusters.get(i)));
            }
            writer.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();;
            throw ex;
        }
    }

    /**
     * Read the events file into a structure.
     * @param eventsFile file with the events.
     * @throws java.lang.Exception any error.
     */
    public static LinkedHashMap<String, ArrayList<String>> readEvents(String eventsFile) throws Exception {

        int index;                                          //index
        String dateStr;                                     //date string
        String valueStr;                                    //value string
        String nextLine;                                    //next line
        ArrayList<String> valueList;                        //value list
        LinkedHashMap<String, ArrayList<String>> dataRows;  //all data rows
        BufferedReader reader;                              //file reader

        try {
            reader = FileLoader.getBufferedInputStream(eventsFile);
            dataRows = new LinkedHashMap<>();
            while ((nextLine = reader.readLine()) != null) {
                index = nextLine.indexOf("[");
                dateStr = nextLine.substring(0, index).trim();
                valueStr = nextLine.substring(index+1, nextLine.length()-1);
                valueList = StringHandler.tokenize(valueStr, ", ", false);
                dataRows.put(dateStr, valueList);
            }

            return dataRows;
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Read the clusters file into a structure.
     * @param clustersFile file with the clusters.
     * @throws java.lang.Exception any error.
     */
    public static ArrayList<Cluster> readClusters(String clustersFile) throws Exception {

        int index;                                          //index
        String nextKey;                                     //next key
        String valueStr;                                    //value string
        String nextLine;                                    //next line
        ArrayList<Cluster> clusters;                        //generated clusters
        Cluster cluster;                                    //single cluster
        BufferedReader reader;                              //file reader

        try {
            reader = FileLoader.getBufferedInputStream(clustersFile);
            clusters = new ArrayList<>();
            while ((nextLine = reader.readLine()) != null) {
                while ((nextLine != null) && !nextLine.startsWith("Cluster")) {
                    nextLine = reader.readLine();
                }

                if (nextLine != null) {
                    nextKey = nextLine.trim();
                    cluster = new Cluster(nextKey);
                    clusters.add(cluster);

                    nextLine = reader.readLine();
                    while ((nextLine != null) && !nextLine.trim().isEmpty()) {
                        index = nextLine.indexOf(DataConst.KEYSEP);
                        nextLine = nextLine.substring(index+1);
                        index = nextLine.indexOf(",");
                        valueStr = nextLine.substring(0, index);
                        cluster.list.add(valueStr.trim());

                        nextLine = reader.readLine();
                    }
                }
            }

            return clusters;
        }
        catch (Exception ex) {
            throw ex;
        }
    }
}

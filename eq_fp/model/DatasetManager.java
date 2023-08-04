/*
 * DatasetManager.java
 *
 * Created on 6 November 2014.
 *
 * Version 1.0
 *
 * Copyright (c) Kieran Greer
 */

package eq_fp.model;


import eq_fp.util.DataConst;
import org.ai_heuristic.eval.EvaluateBase;
import org.ai_heuristic.util.AiHeuristicConst;
import org.ai_heuristic.util.SymbolHandler;
import org.jlog2.Logger;
import org.jlog2.LoggerHandler;
import org.jlog2.exception.ExceptionHandler;
import org.jlog2.util.FileLoader;
import org.jlog2.util.StringHandler;

import java.util.ArrayList;


/**
 * Reads a data file to create a tabular model of data rows.
 * @author Kieran Greer
 */
public class DatasetManager 
{
    /** For logging */
    private static final Logger logger;
       
    
    /** The dataset file path */
    protected String datasetFile;
    
    /** The data points type */
    protected String dataType;
    
    /** Input dataset to evaluate. Each element is another ArrayList of data values */
    protected ArrayList inDatasets;
    
    /** Output dataset to match to. Each element is another ArrayList of data values */
    protected ArrayList outDatasets;
        
    
    static
    {
        // get the logger
        logger = LoggerHandler.getLogger(DatasetManager.class.getName());
        logger.setDebug(true);
    }
    
    
    /**
     * Create a new instance of DatasetManager.
     * @param filePath the path to the dataset file.
     */
    public DatasetManager(String filePath)
    {
        datasetFile = filePath;
        dataType = null;
        inDatasets = new ArrayList();
        outDatasets = new ArrayList();
    }

    /**
     * Read the data file and create the dataset structures.
     * @return true if created OK, false if some error.
     * @throws Exception any other error.
     */
    public boolean createDataset() throws Exception
    {
        int i, j, k;
        int nextIndex;                              //next index
        int start;                                  //start column
        int end;                                    //end column
        int number, counter;                        //counter
        String input;                               //input text
        String nextToken;                           //next token
        String nextDescr;                           //next description
        String nextLine;                            //next line
        String tokenizer;                           //point tokenizer
        ArrayList inputCols;                        //input columns
        ArrayList outputCols;                       //output columns
        ArrayList wordList;                         //list of words
        ArrayList lineList;                         //all lines
        ArrayList valueList;                        //list of values
        ArrayList valueList2;                       //list of values
        ArrayList dataset;                          //all data rows

        try
        {
            input = FileLoader.getInputStream(datasetFile);
            tokenizer = SymbolHandler.CA;
            dataset = new ArrayList();
            inputCols = new ArrayList();
            outputCols = new ArrayList();

            //tokenize lines and retrieve the metadata
            counter = 0;
            lineList = StringHandler.tokenize(input, SymbolHandler.NL, false);

            for (i = 0; i < 4; i++)
            {
                nextLine = (String)lineList.get(i);
                nextDescr = null;
                wordList = StringHandler.tokenize(nextLine, SymbolHandler.CL, false);
                nextToken = (String)wordList.get(0);
                if (wordList.size() > 1) nextDescr = (String)wordList.get(1);

                if (nextToken.equalsIgnoreCase("Input") || nextToken.endsWith("Input"))
                {
                    if (nextDescr != null)
                    {
                        valueList = StringHandler.tokenize(nextDescr, SymbolHandler.CA, false);
                        for (j = 0; j < valueList.size(); j++)
                        {
                            nextToken = (String)valueList.get(j);
                            if (nextToken.contains("-"))
                            {
                                valueList2 = StringHandler.tokenize(nextDescr, "-", false);
                                start = Integer.parseInt((String)valueList2.get(0));
                                end = Integer.parseInt((String)valueList2.get(1));

                                for (k = start; k <= end; k++)
                                {
                                    inputCols.add(k);
                                }
                            }
                            else
                            {
                                number = Integer.parseInt(nextToken.trim());
                                inputCols.add(number);
                            }
                        }
                    }

                    counter++;
                }
                else if (nextToken.equalsIgnoreCase(DataConst.OUTPUT))
                {
                    if (nextDescr != null)
                    {
                        valueList = StringHandler.tokenize(nextDescr, SymbolHandler.CA, false);
                        for (j = 0; j < valueList.size(); j++)
                        {
                            nextToken = (String)valueList.get(j);
                            if (nextToken.contains("-"))
                            {
                                valueList2 = StringHandler.tokenize(nextToken, "-", false);
                                start = Integer.parseInt((String)valueList2.get(0));
                                end = Integer.parseInt((String)valueList2.get(1));

                                for (k = start; k <= end; k++)
                                {
                                    outputCols.add(k);
                                }
                            }
                            else
                            {
                                start = Integer.parseInt(nextToken.trim());
                                outputCols.add(start);
                            }
                        }
                    }

                    counter++;
                }
                else if (nextToken.equalsIgnoreCase(AiHeuristicConst.TYPE))
                {
                    if (nextDescr != null) dataType = nextDescr.trim();
                    counter++;
                }
                else if (nextToken.equalsIgnoreCase(DataConst.TOKENIZER))
                {
                    if (nextDescr != null) tokenizer = nextDescr.trim();
                    counter++;
                }
                else
                {
                    break;
                }
            }

            //add the data points to lists
            for (i = counter; i < lineList.size(); i++)
            {
                nextLine = (String)lineList.get(i);
                wordList = StringHandler.tokenize(nextLine, tokenizer, false);

                valueList = new ArrayList();
                for (j = 0; j < wordList.size(); j++)
                {
                    nextToken = (String)wordList.get(j);
                    try
                    {
                        valueList.add(EvaluateBase.valueFromString(dataType, nextToken));
                    }
                    catch (NumberFormatException nfex)
                    {
                        valueList.add(EvaluateBase.valueFromString(String.class.getName(), nextToken));
                    }
                }

                dataset.add(valueList);
            }

            //add the data points to the input/output datasets
            for (i = 0; i < dataset.size(); i++)
            {
                valueList = (ArrayList)dataset.get(i);
                if (!inputCols.isEmpty())
                {
                    try {
                        valueList2 = new ArrayList();
                        for (j = 0; j < inputCols.size(); j++) {
                            nextIndex = ((Integer) inputCols.get(j)).intValue();
                            valueList2.add(valueList.get(nextIndex));
                        }
                        inDatasets.add(valueList2);
                    }
                    catch (IndexOutOfBoundsException iob) {
                        System.out.println("A data row may not be complete and has not been included.");
                    }
                    catch (Exception eee)
                    {
                        //also flag other errors
                        eee.printStackTrace();
                    }
                }

                if (!outputCols.isEmpty())
                {
                    valueList2 = new ArrayList();
                    for (j = 0; j < outputCols.size(); j++)
                    {
                        nextIndex = ((Integer)outputCols.get(j)).intValue();
                        valueList2.add(valueList.get(nextIndex));
                    }
                    outDatasets.add(valueList2);
                }
            }

            return true;
        }
        catch (Exception ex)
        {
            throw ExceptionHandler.handleException(logger, LoggerHandler.ERROR, "createDataset",
                    "", ex);
        }
    }
    
    /**
     * Get the input dataset. Each element is a ArrayList of data points.
     * @return the value of inDatasets.
     */
    public ArrayList getInputDatasets()
    {
        return inDatasets;
    }
    
    /**
     * Get the output dataset. Each element is a ArrayList of data points.
     * @return the value of outDatasets.
     */
    public ArrayList getOutputDatasets()
    {
        return outDatasets;
    }
}

/*
 * DataConst.java
 *
 * Created on 1 September 2021
 *
 * Version 1.0
 *
 * Copyright (c) Kieran Greer
 */

package eq_fp.util;


import org.jlog2.util.StringHandler;

import java.util.ArrayList;

/**
 * Constant values for the data processing.
 * @author Kieran Greer
 */
public class DataConst
{
    /** File base */
    public static final String DIRROOT = "../../";
    public static final String DATAROOT = (DIRROOT + "data/");
    public static final String TESTROOT = (DIRROOT + "test/");

    /** Defines input type tag */
    public static final String INPUT = "Input";
    
    /** Defines output type tag */
    public static final String OUTPUT = "Output";
    
    /** Defines tokenizer type tag */
    public static final String TOKENIZER = "Tokenizer";

    /** Defines key separator tag */
    public static final String KEYSEP = ":";

    /**
     * Convert the single key into it parts.
     * @param dataKey full compound data key.
     * @return separate key parts.
     */
    public static ArrayList<String> fromDataKey(String dataKey) {
        return StringHandler.tokenize(dataKey, DataConst.KEYSEP, false);
    }
}

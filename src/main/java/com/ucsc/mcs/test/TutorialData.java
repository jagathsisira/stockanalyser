package com.ucsc.mcs.test;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.tools.data.FileHandler;

import java.io.File;
import java.io.IOException;

/**
 * Created by JagathA on 12/25/2017.
 */
public enum TutorialData {
    IRIS("devtools/data/iris.data", 4, ",",null), SPARSE(
            "devtools/data/sparse.tsv", 0, " ",":");

    private String file;
    private int classIndex;
    private String sep;
    private String sep2;

    private TutorialData(String file, int classindex, String sep, String sep2) {
        this.file = file;
        this.classIndex = classindex;
        this.sep = sep;
        this.sep2 = sep2;
    }

    public Dataset load() throws IOException {
        if (sep2 == null)
            return FileHandler.loadDataset(new File(file), classIndex, sep);
        else
            return FileHandler.loadSparseDataset(new File(file), classIndex,
                    sep, sep2);
    }
}

package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.AnnouncementData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JagathA on 1/20/2018.
 */
public class TfIdfAnnouncementData extends AnnouncementData {

    private List<String> document;

    public TfIdfAnnouncementData(String exchange, String symbol, String annDate, int trend, int weight, ArrayList<String>
            document) {
        super(exchange, symbol, annDate, trend, weight);
    }

    public List<String> getDocument() {
        return document;
    }

    public void setDocument(List<String> document) {
        this.document = document;
    }
}

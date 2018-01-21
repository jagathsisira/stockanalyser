package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.NewsData;

import java.util.List;

/**
 * Created by JagathA on 1/20/2018.
 */
public class TfIdfNewsData extends NewsData {

    private List<String> document;

    public TfIdfNewsData(NewsData newsData, List<String> document) {
        super(newsData.getExchange(), newsData.getSymbol(), newsData.getNewsDate(), newsData.getTrend(), newsData
                .getWeight());
        this.document = document;
    }

    public List<String> getDocument() {
        return document;
    }

    public void setDocument(List<String> document) {
        this.document = document;
    }
}

package com.ucsc.mcs.impl.tfidf.connector;

import java.util.List;

/**
 * Created by JagathA on 1/21/2018.
 */
public class WeightedDocument {

    private List<String> document;
    private int weight;

    public WeightedDocument(List<String> document, int weight) {
        this.document = document;
        this.weight = weight;
    }

    public List<String> getDocument() {
        return document;
    }

    public void setDocument(List<String> document) {
        this.document = document;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}

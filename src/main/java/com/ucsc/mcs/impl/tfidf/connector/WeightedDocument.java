package com.ucsc.mcs.impl.tfidf.connector;

import java.io.Serializable;
import java.util.List;

/**
 * Created by JagathA on 1/21/2018.
 */
public class WeightedDocument implements Serializable, Cloneable {

    private List<String> document;
    private int weight;
    private String id;

    public WeightedDocument(List<String> document, int weight, String id) {
        this.document = document;
        this.weight = weight;
        this.id = id;
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

    public String getId() {
        return id;
    }
}

package com.ucsc.mcs.impl.data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by JagathA on 11/13/2017.
 */
public class AnnouncementData implements Serializable, Cloneable {

    private int trend;
    private int weight;
    private String exchange;
    private String symbol;
    private String annDate;
    private String annHeading;
    private String annBody;
    private String dataModelInput;

    public AnnouncementData(String exchange, String symbol, String annDate, int trend, int weight) {
        this.exchange = exchange;
        this.symbol = symbol;
        this.annDate = annDate;
        this.trend = trend;
        this.weight = weight;
    }

    public String getExchange() {
        return exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getAnnDate() {
        return annDate;
    }

    public String getAnnHeading() {
        return annHeading;
    }

    public String getAnnBody() {
        return annBody;
    }

    public int getTrend() {
        return trend;
    }

    public int getWeight() {
        return weight;
    }

    public void setAnnHeading(String annHeading) {
        this.annHeading = annHeading;
    }

    public void setAnnBody(String annBody) {
        this.annBody = annBody;
    }

    public String getDataModelInput() {
        return dataModelInput;
    }

    public void setDataModelInput(String dataModelInput) {
        this.dataModelInput = dataModelInput;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

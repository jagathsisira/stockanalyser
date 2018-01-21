package com.ucsc.mcs.impl.data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by JagathA on 11/13/2017.
 */
public class NewsData implements Serializable, Cloneable {

    private int trend;
    private int weight;
    private String exchange;
    private String symbol;
    private String newsDate;
    private String newsHeading;
    private String newsBody;
    private String dataModelInput;

    public NewsData(String exchange, String symbol, String newsDate, int trend, int weight) {
        this.exchange = exchange;
        this.symbol = symbol;
        this.newsDate = newsDate;
        this.trend = trend;
        this.weight = weight;
    }

    public String getExchange() {
        return exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getNewsDate() {
        return newsDate;
    }

    public int getTrend() {
        return trend;
    }

    public int getWeight() {
        return weight;
    }

    public String getNewsHeading() {
        return newsHeading;
    }

    public String getNewsBody() {
        return newsBody;
    }

    public void setNewsHeading(String newsHeading) {
        this.newsHeading = newsHeading;
    }

    public void setNewsBody(String newsBody) {
        this.newsBody = newsBody;
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

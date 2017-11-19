package com.ucsc.mcs.impl.data;

/**
 * Created by JagathA on 11/13/2017.
 */
public class NewsData {

    private String exchange;
    private String symbol;
    private String newsDate;
    private String newsHeading;
    private String newsBody;
    private int trend;
    private int weight;

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
}

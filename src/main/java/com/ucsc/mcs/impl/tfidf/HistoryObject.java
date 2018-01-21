package com.ucsc.mcs.impl.tfidf;

/**
 * Created by JagathA on 11/12/2017.
 */
public class HistoryObject {

    private String exchange;
    private String symbol;
    private String date;
    private double close;

    public HistoryObject(String exchange, String symbol, String date, double close) {
        this.exchange = exchange;
        this.symbol = symbol;
        this.date = date;
        this.close = close;
    }

    public String getExchange() {
        return exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDate() {
        return date;
    }

    public double getClose() {
        return close;
    }
}

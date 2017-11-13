package com.ucsc.mcs.impl.data;

/**
 * Created by JagathA on 11/12/2017.
 */
public class HotSpot {

    private boolean isAnnouncementsAvailable;
    private boolean isNewsAvailable;
    private int currentTrend;
    private int prevTrend;
    private int weight;
    private String exchange;
    private String symbol;
    private String date;

    public HotSpot(String exchange, String symbol, String date, int currentTrend, int prevTrend, int weight,
                   boolean isAnnouncementsAvailable, boolean isNewsAvailable) {
        this.exchange = exchange;
        this.symbol = symbol;
        this.date = date;
        this.currentTrend = currentTrend;
        this.prevTrend = prevTrend;
        this.weight = weight;
        this.isAnnouncementsAvailable = isAnnouncementsAvailable;
        this.isNewsAvailable = isNewsAvailable;
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

    public int getCurrentTrend() {
        return currentTrend;
    }

    public int getPrevTrend() {
        return prevTrend;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isAnnouncementsAvailable() {
        return isAnnouncementsAvailable;
    }

    public boolean isNewsAvailable() {
        return isNewsAvailable;
    }
}

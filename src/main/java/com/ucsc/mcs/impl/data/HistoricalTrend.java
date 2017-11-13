package com.ucsc.mcs.impl.data;

/**
 * Created by JagathA on 11/12/2017.
 */
public class HistoricalTrend {

    private int trend;
    private int weight;

    public HistoricalTrend(int trend, int weight) {
        this.trend = trend;
        this.weight = weight;
    }

    public int getTrend() {
        return trend;
    }

    public int getWeight() {
        return weight;
    }
}

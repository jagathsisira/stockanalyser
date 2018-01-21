package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by JagathA on 11/11/2017.
 */
public class HotSpotInspector {
    private SqlConnector sqlConnector = null;
    private HashMap<String, ArrayList<HotSpot>> hotSpots = null;

    public HotSpotInspector(SqlConnector connector) {
        this.sqlConnector = connector;
        this.hotSpots = new HashMap<String, ArrayList<HotSpot>>();
    }

    public HashMap<String, ArrayList<HotSpot>> getHotSpots() {
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select exchange, symbol from msc.history group by exchange, symbol;";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String exchange = rs.getString(1);
                    String symbol = rs.getString(2);
                    System.out.println("Keys " + exchange + " : " + symbol);
                    populateHistory(exchange, symbol);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }
        System.out.println("++++ " + hotSpots);
        return hotSpots;
    }

    private void populateHistory(String exchange, String symbol) {
        ArrayList<HistoryObject> symbolHistoryList = new ArrayList<HistoryObject>();
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select str_to_date(HIST_DATE, '%Y-%m-%d 00:00:00') as date, close from history where exchange=? and symbol=? order by date asc";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                statement.setString(1, exchange);
                statement.setString(2, symbol);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String date = rs.getString(1);
                    double closePrice = rs.getDouble(2);
                    symbolHistoryList.add(new HistoryObject(exchange, symbol, date, closePrice));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        System.out.println("History List size for  : " + exchange + " : " + symbol + " Size = " + symbolHistoryList.size());

        findHotSpot(symbolHistoryList, exchange, symbol);
        System.out.println("### hotspot list size : " + hotSpots.size());
    }

    private void findHotSpot(ArrayList<HistoryObject> historyList, String exchange, String symbol) {
        int position = 4; //starting from 4th position to compare 4 pair of dates
        ArrayList<String> annDates = getAnnDates(exchange, symbol);
        ArrayList<String> newsDates = getNewsDates(exchange, symbol);

        while (position <= (historyList.size() - 1)) {
            HistoryObject currentObject = historyList.get(position);
            HistoryObject previousObject = historyList.get(position - 1);

            int currentTrend = getTrend(currentObject, previousObject);
            HistoricalTrend previousTrend = getPreviousTrend(position, historyList);

//            System.out.println("History trend  : " + previousTrend.getTrend() + " : " + previousTrend.getWeight());

            if ((currentTrend != 0 && previousTrend.getTrend() != 0) &&
                    (currentTrend > previousTrend.getTrend() || currentTrend < previousTrend.getTrend())) {
                ArrayList<HotSpot> exchangeHotSpots = hotSpots.get(currentObject.getExchange());

                if(exchangeHotSpots == null){
                    exchangeHotSpots = new ArrayList<>();
                    hotSpots.put(currentObject.getExchange(), exchangeHotSpots);
                }

                boolean isAnnAvailable = annDates.contains(previousObject.getDate());
                boolean isNewsAvailable = newsDates.contains(previousObject.getDate());

                HotSpot hotSpot = new HotSpot(currentObject.getExchange(), currentObject.getSymbol(), previousObject.getDate()
                        , currentTrend, previousTrend.getTrend(), previousTrend.getWeight()
                        , isAnnAvailable, isNewsAvailable);

                exchangeHotSpots.add(hotSpot);

                if(isAnnAvailable || isNewsAvailable) {
                    addHotSpotData(hotSpot);
                }

                System.out.println("**** Hotspot Added  : " + currentTrend + " : " + previousTrend.getTrend() + " : "
                        + previousTrend.getWeight() + " : " + previousObject.getDate());
            }
            position++;
        }

    }

    private int getTrend(HistoryObject currentObj, HistoryObject prevObj) {
        int currentTrend = 0;
        double priceChange = (currentObj.getClose() - prevObj.getClose());

        if (priceChange > 0) {
            currentTrend = 1;
        } else if (priceChange < 0) {
            currentTrend = -1;
        }

//        System.out.println("Current trend  : " + currentObj.getExchange() + " : " + currentObj.getSymbol() + " : "
//                + currentTrend + " : " + prevObj.getClose() + " " + currentObj.getClose());

        return currentTrend;
    }

    private HistoricalTrend getPreviousTrend(int position, ArrayList<HistoryObject> historyObjects) {
        int prevTrend = 0;
        int weight = 0;

        double priceChangeDay1 = (historyObjects.get(position - 1).getClose()) - (historyObjects.get(position - 2).getClose());
        double priceChangeDay2 = (historyObjects.get(position - 2).getClose()) - (historyObjects.get(position - 3).getClose());
        double priceChangeDay3 = (historyObjects.get(position - 3).getClose()) - (historyObjects.get(position - 4).getClose());

        if (priceChangeDay1 > 0) {
            prevTrend = 1;
            weight = 1;

            if (priceChangeDay2 > 0) {
                prevTrend = 1;
                weight = 2;

                if (priceChangeDay3 > 0) {
                    prevTrend = 1;
                    weight = 3;
                }
            }
        } else if (priceChangeDay1 < 0) {
            prevTrend = -1;
            weight = 1;

            if (priceChangeDay2 < 0) {
                prevTrend = -1;
                weight = 2;

                if (priceChangeDay3 < 0) {
                    prevTrend = -1;
                    weight = 3;
                }
            }
        }

        return new HistoricalTrend(prevTrend, weight);
    }

    private ArrayList<String> getAnnDates(String exchange, String symbol){
        ArrayList<String> annsDates = new ArrayList<String>();
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

//            String createTableSQL = "select distinct(str_to_date(ANN_DATE, '%d-%M-%Y')) from msc.announcements where  exchange = ? and symbol = ?;";
            String createTableSQL = "select distinct(str_to_date(ANN_DATE, '%d-%b-%y')) from msc.announcements where  exchange = ? and symbol = ?;";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                statement.setString(1, exchange);
                statement.setString(2, symbol);
                ResultSet rs = statement.executeQuery();


                while (rs.next()) {
                    System.out.println("Ann Date " + rs.getString(1));
                    annsDates.add(rs.getString(1));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        System.out.println("Ann Size for : " + exchange + " " + symbol + " : " + annsDates.size());
        return annsDates;
    }

    private ArrayList<String> getNewsDates(String exchange, String symbol){
        ArrayList<String> newsDates = new ArrayList<String>();
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select distinct(str_to_date(NEWS_DATE, '%Y-%m-%d')) from msc.news where exchange = ? and symbol = ?;";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                statement.setString(1, exchange);
                statement.setString(2, symbol);
                ResultSet rs = statement.executeQuery();


                while (rs.next()) {
                    System.out.println("News Date " + rs.getString(1));
                    newsDates.add(rs.getString(1));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        System.out.println("News Size for : " + exchange + " " + symbol + " : " + newsDates.size());
        return newsDates;
    }

    private boolean addHotSpotData(HotSpot hotSpot){
        boolean isNewsAvailable = false;
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "insert into msc.hotspots values (?,?,?,?,?,?,?,?)";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                statement.setString(1, hotSpot.getExchange());
                statement.setString(2, hotSpot.getSymbol());
                statement.setString(3, hotSpot.getDate());
                statement.setDouble(4, hotSpot.getCurrentTrend());
                statement.setDouble(5, hotSpot.getPrevTrend());
                statement.setInt(6, hotSpot.getWeight());
                statement.setBoolean(7, hotSpot.isAnnouncementsAvailable());
                statement.setBoolean(8, hotSpot.isNewsAvailable());
                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        System.out.println("Hot Spot Added: ");
        return isNewsAvailable;
    }

}

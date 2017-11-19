package com.ucsc.mcs.impl.datamanage;

import com.ucsc.mcs.impl.connector.SqlConnector;
import com.ucsc.mcs.impl.data.NewsData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by JagathA on 11/13/2017.
 */
public class NewsClassifier {

    private SqlConnector sqlConnector = null;
    private ArrayList<NewsData> newsList = null;

    public NewsClassifier(SqlConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    public void classifyNews(){
        loadNews();
        countNewsWords();
    }

    private void loadNews(){
        Connection dbConnection = sqlConnector.connect();
        newsList = new ArrayList<NewsData>();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select exchange, symbol, spot_date, current_trend, weight from msc.hotspots where is_news_avail=1";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String exchange = rs.getString(1);
                    String symbol = rs.getString(2);
                    String spotDate = rs.getString(3);
                    int trend = rs.getInt(4);
                    int weight = rs.getInt(5);
                    System.out.println("News " + exchange + " : " + symbol + " : " + spotDate + " : " + trend + " : " + weight);
                    newsList.add(new NewsData(exchange, symbol, spotDate, trend, weight));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        System.out.println("News List count " + newsList.size());
    }

    private void countNewsWords(){
        Connection dbConnection = sqlConnector.connect();

        for(NewsData newsData : newsList) {
            if (dbConnection != null) {
                PreparedStatement statement = null;

                String createTableSQL = "select heading, body from msc.news where str_to_date(NEWS_DATE, '%m/%d/%Y') = ? and exchange = ? and symbol = ?";
                try {
                    statement = dbConnection.prepareStatement(createTableSQL);
                    statement.setString(1, newsData.getNewsDate());
                    statement.setString(2, newsData.getExchange());
                    statement.setString(3, newsData.getSymbol());
                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        String heading = rs.getString(1);
                        String body = rs.getString(2);
                        System.out.println("News details : " + heading + " : " +body );
                        newsData.setNewsHeading(heading);
                        newsData.setNewsBody(body);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                }
            }
        }


    }
}

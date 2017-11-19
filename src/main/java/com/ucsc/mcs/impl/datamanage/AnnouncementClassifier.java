package com.ucsc.mcs.impl.datamanage;

import com.ucsc.mcs.impl.connector.SqlConnector;
import com.ucsc.mcs.impl.data.AnnouncementData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by JagathA on 11/13/2017.
 */
public class AnnouncementClassifier {
    private SqlConnector sqlConnector = null;
    private ArrayList<AnnouncementData> annsList = null;

    public AnnouncementClassifier(SqlConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    public void classifyNews(){
        loadAnnouncements();
        countAnnsWords();
    }

    private void loadAnnouncements(){
        Connection dbConnection = sqlConnector.connect();
        annsList = new ArrayList<AnnouncementData>();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select exchange, symbol, spot_date, current_trend, weight from msc.hotspots where is_ann_avail=1";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String exchange = rs.getString(1);
                    String symbol = rs.getString(2);
                    String spotDate = rs.getString(3);
                    int trend = rs.getInt(4);
                    int weight = rs.getInt(5);
                    System.out.println("Anns " + exchange + " : " + symbol + " : " + spotDate + " : " + trend + " : " + weight);
                    annsList.add(new AnnouncementData(exchange, symbol, spotDate, trend, weight));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        System.out.println("Anns List count " + annsList.size());
    }

    private void countAnnsWords(){
        Connection dbConnection = sqlConnector.connect();

        for(AnnouncementData announcementData : annsList) {
            if (dbConnection != null) {
                PreparedStatement statement = null;

                String createTableSQL = "select heading, body from msc.announcements where str_to_date(ANN_DATE, '%m/%d/%Y') = ? and exchange = ? and symbol = ?";
                try {
                    statement = dbConnection.prepareStatement(createTableSQL);
                    statement.setString(1, announcementData.getAnnDate());
                    statement.setString(2, announcementData.getExchange());
                    statement.setString(3, announcementData.getSymbol());
                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        String heading = rs.getString(1);
                        String body = rs.getString(2);
                        System.out.println("Anns details : " + heading + " : " +body );
                        announcementData.setAnnHeading(heading);
                        announcementData.setAnnBody(body);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                }
            }
        }


    }
}

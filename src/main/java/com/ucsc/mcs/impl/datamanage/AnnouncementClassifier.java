package com.ucsc.mcs.impl.datamanage;

import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.tfidf.TextEntry;
import com.ucsc.mcs.impl.tfidf.TextClassificationStore;
import com.ucsc.mcs.impl.tfidf.TextUtils;
import com.uttesh.exude.ExudeData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by JagathA on 11/13/2017.
 */
public class AnnouncementClassifier {
    private SqlConnector sqlConnector = null;

    public AnnouncementClassifier(SqlConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    public void classifyAnnouncements(){
        loadAnnouncements();
        countAnnsWords();
        dumpAnnouncements();
    }

    private void loadAnnouncements(){
        Connection dbConnection = sqlConnector.connect();
        int count = 0;

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select exchange, symbol, spot_date, current_trend, weight from msc2.hotspots where is_ann_avail=1";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String exchange = rs.getString(1);
                    String symbol = rs.getString(2);
                    String spotDate = rs.getString(3);
                    int trend = rs.getInt(4);
                    int weight = rs.getInt(5);
                    TextClassificationStore.getInstance().getAnnouncementsList().add(new AnnouncementData("0",
                            exchange,
                            symbol, spotDate, trend, weight));
                    count ++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        System.out.println("Anns List count " + TextClassificationStore.getInstance().getAnnouncementsList().size());
    }

    private void countAnnsWords(){
        Connection dbConnection = sqlConnector.connect();

        for(AnnouncementData announcementData : TextClassificationStore.getInstance().getAnnouncementsList()) {
            if (dbConnection != null) {
                PreparedStatement statement = null;

                String createTableSQL = "select heading, body from msc.announcements where str_to_date(ANN_DATE, '%d-%b-%y') = ? and exchange = ? and symbol = ?";
                try {
                    statement = dbConnection.prepareStatement(createTableSQL);
                    statement.setString(1, announcementData.getAnnDate());
                    statement.setString(2, announcementData.getExchange());
                    statement.setString(3, announcementData.getSymbol());
                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        String heading = rs.getString(1).toLowerCase();
                        String body = rs.getString(2).toLowerCase();
                        announcementData.setDataModelInput(String.join(" ", TextUtils.parseSentences(ExudeData
                                .getInstance().filterStoppingsKeepDuplicates(heading))));
                        updateTextClassifier(announcementData);
                        announcementData.setAnnHeading(heading);
                        announcementData.setAnnBody(body);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            }
        }
    }

    private void updateTextClassifier(AnnouncementData annData){
        String[] words = annData.getDataModelInput().split(" ");
        String refactoredWord = "";

        for(String word : words){
            refactoredWord = word.toLowerCase().replaceAll("[0-9]", "");
            if(refactoredWord.length() > 1) {
                TextEntry textEntry = new TextEntry(refactoredWord.toLowerCase());
                textEntry.setScore(annData.getWeight() * annData.getTrend());
                TextClassificationStore.getInstance().addTextClassificationForText(refactoredWord.toLowerCase(), textEntry);
            }
        }
    }

    private void dumpAnnouncements(){
        TextClassificationStore.getInstance().dumpAnnouncements();
    }
}

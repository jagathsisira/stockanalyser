package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.ucsc.mcs.impl.tfidf.connector.WeightedDocument;
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JagathA on 1/20/2018.
 */
public class TfIdfAnnouncementClassifier {

    private static final Logger logger = Logger.getLogger(TfIdfAnnouncementClassifier.class);

    private SqlConnector sqlConnector = null;

    public TfIdfAnnouncementClassifier(SqlConnector sqlConnector){
        this.sqlConnector = sqlConnector;
    }

    public void classifyAnnouncements() {
        this.loadAnnouncements();
        this.dumpAnnouncements();
        this.calculateTermWeights();
//        this.calculateTfIdfValues();
    }

    private void calculateTermWeights(){
        for(AnnouncementData annData : TextClassificationStore.getInstance().loadAnnouncementsFromFile()){
            try {

                List<String> document = TextUtils.parseSentences(ExudeData
                                .getInstance().filterStoppingsKeepDuplicates(annData.getAnnHeading()),
                        true);
//                System.out.println("Original : " + document.toString());
//                System.out.println("Updated : " + TextUtils.removeDuplicates(document));
                TextClassificationStore.getWeightedDocumentList().add(new WeightedDocument(document, (annData
                        .getWeight() * annData.getTrend()), annData.getAnnId()));
//                tfIdfNewsDataArrayList.add(new TfIdfNewsData(newsData, document));
            } catch (InvalidDataException e) {
                logger.error("Error data Ann : " + annData.getAnnId());
            }
        }

        logger.info("+++++++++++ Weighted Docs Size: After Ann : " + TextClassificationStore.getWeightedDocumentList()
                .size());
    }

//    private void calculateTfIdfValues(){
//        for(TfIdfNewsData newsData : tfIdfNewsDataArrayList){
//            for(String term : newsData.getDocument()){
//                double tfIdf = TFIDFCalculator.getInstance().tfIdf(newsData.getDocument(), documentList, term);
//                System.out.println("Term: " + term + " : TFIDF : " + tfIdf);
//                TextEntry textEntry = new TextEntry(term);
//                textEntry.setScore(newsData.getTrend() * newsData.getWeight() * tfIdf);
//                TextClassificationStore.getInstance().addTextClassificationForText(term, textEntry, false);
//            }
//        }
//    }

    private void loadAnnouncements() {
        int count = 0;
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select exchange, symbol, spot_date, current_trend, weight from msc.hotspots " +
                    "where is_ann_avail=1";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String exchange = rs.getString(1);
                    String symbol = rs.getString(2);
                    String spotDate = rs.getString(3);
                    int trend = rs.getInt(4);
                    int weight = rs.getInt(5);
//                    TextClassificationStore.getInstance().getAnnouncementsList().add(new AnnouncementData(exchange, symbol, spotDate,
//                            trend,
//                            weight));
                    countAnnouncementsWords(exchange, symbol, spotDate, trend, weight);
                    count++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        logger.info("Ann List count " + TextClassificationStore.getInstance().getAnnouncementsList().size());
    }

    private void countAnnouncementsWords(String exchange, String symbol, String date, int trend, int weight) {
        Connection dbConnection = sqlConnector.connect();

            if (dbConnection != null) {
                PreparedStatement statement = null;

                String createTableSQL = "select heading, body, ann_id from msc.announcements where str_to_date" +
                        "(ANN_DATE, '%d-%b-%y') = ? and exchange = ? and symbol = ?";
                try {
                    statement = dbConnection.prepareStatement(createTableSQL);
                    statement.setString(1, date);
                    statement.setString(2, exchange);
                    statement.setString(3, symbol);
                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {

                        String heading = rs.getString(1).toLowerCase();
                        String body = rs.getString(2).toLowerCase();
                        int annId = rs.getInt(3);
                        AnnouncementData announcementData = new AnnouncementData(""+annId, exchange, symbol, date,
                                trend,
                                weight);
                        announcementData.setDataModelInput(String.join(" ", TextUtils.parseSentences(ExudeData.getInstance()
                                .filterStoppingsKeepDuplicates(heading))));
                        announcementData.setAnnHeading(heading);
                        announcementData.setAnnBody(body);
                        TextClassificationStore.getInstance().getAnnouncementsList().add(announcementData);
                        logger.info("Ann loaded: " + annId + " : " + exchange + " : " + symbol + " : " + weight + " :" +
                                " " + trend);
                        updateTextClassifier(announcementData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            }
    }

//    private void countAnnouncementsWords() {
//        Connection dbConnection = sqlConnector.connect();
//
//        for (AnnouncementData announcementData : TextClassificationStore.getInstance().getAnnouncementsList()) {
//            if (dbConnection != null) {
//                PreparedStatement statement = null;
//
//                String createTableSQL = "select heading, body from msc.announcements where str_to_date(ANN_DATE, '%d-%b-%y') = ? and exchange = ? and symbol = ?";
//                try {
//                    statement = dbConnection.prepareStatement(createTableSQL);
//                    statement.setString(1, announcementData.getAnnDate());
//                    statement.setString(2, announcementData.getExchange());
//                    statement.setString(3, announcementData.getSymbol());
//                    ResultSet rs = statement.executeQuery();
//
//                    while (rs.next()) {
//                        String heading = rs.getString(1).toLowerCase();
//                        String body = rs.getString(2).toLowerCase();
//                        announcementData.setDataModelInput(String.join(" ", TextUtils.parseSentences(ExudeData.getInstance()
//                                .filterStoppingsKeepDuplicates(heading))));
//                        announcementData.setAnnHeading(heading);
//                        announcementData.setAnnBody(body);
//                        updateTextClassifier(announcementData);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                }
//            }
//        }
//    }

    private void updateTextClassifier(AnnouncementData newsData) {
        String[] words = newsData.getDataModelInput().split(" ");
        String refactoredWord = "";

        for (String word : words) {
            refactoredWord = word.toLowerCase().replaceAll("[0-9]", "");
            if (refactoredWord.length() > 1) {
                TextEntry textEntry = new TextEntry(refactoredWord.toLowerCase());
                textEntry.setScore(newsData.getWeight() * newsData.getTrend());
                TextClassificationStore.getInstance().addTextClassificationForText(refactoredWord.toLowerCase(), textEntry);
            }
        }
    }

    private void dumpAnnouncements(){
        TextClassificationStore.getInstance().dumpAnnouncements();
    }
}

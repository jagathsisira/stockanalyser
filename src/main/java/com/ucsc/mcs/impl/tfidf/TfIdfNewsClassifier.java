package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.NewsData;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.ucsc.mcs.impl.tfidf.connector.WeightedDocument;
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by JagathA on 1/20/2018.
 */
public class TfIdfNewsClassifier {

    final static Logger logger = Logger.getLogger(TfIdfNewsClassifier.class);

    private SqlConnector sqlConnector = null;

    public TfIdfNewsClassifier(SqlConnector sqlConnector){
        this.sqlConnector = sqlConnector;
    }

    public void classifyNews(int predictionStart, int predictionFinish) {
//        this.loadNews();
//        this.dumpNews();
        this.calculateTermWeights(predictionStart, predictionFinish);
//        this.calculateTfIdfValues();
    }

    private void calculateTermWeights(int predictionStart, int predictionFinish){
        int count = 0;
        int processedCount = 0;

        for(NewsData newsData : TextClassificationStore.getInstance().loadNewsFromFile()){
            count ++;
            if(count > predictionStart && count < predictionFinish){
                continue;
            }
            try {

                List<String> document = TextUtils.parseSentences(ExudeData
                        .getInstance().filterStoppingsKeepDuplicates(newsData.getNewsHeading()),
                        true);
//                System.out.println("Original : " + document.toString());
//                System.out.println("Updated : " + TextUtils.removeDuplicates(document));
                TextClassificationStore.getWeightedDocumentList().add(new WeightedDocument(document, (newsData
                        .getWeight()), newsData.getNewsId()));

                processedCount ++;
//                logger.info("Weighted Doc : " + count + " " + newsData.getNewsId() + " " + document.toString());

//                tfIdfNewsDataArrayList.add(new TfIdfNewsData(newsData, document));
            } catch (InvalidDataException e) {
                logger.error("Error data News : " + newsData.getNewsId());
            }
        }

        logger.info("+++++++++++ Weighted Docs Size : after news :  " + predictionStart + " : " + predictionFinish +
                " Processed Count : " + processedCount + " Store Size : " +  TextClassificationStore
                .getWeightedDocumentList().size());
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

    private void loadNews() {
        int count = 0;
        Connection dbConnection = sqlConnector.connect();

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select exchange, symbol, spot_date, current_trend, weight from msc2.hotspots where is_news_avail=1";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String exchange = rs.getString(1);
                    String symbol = rs.getString(2);
                    String spotDate = rs.getString(3);
                    int trend = rs.getInt(4);
                    int weight = rs.getInt(5);
                    countNewsWords(exchange, symbol, spotDate, trend, weight);
//                    TextClassificationStore.getInstance().getNewsList().add(new NewsData(exchange, symbol, spotDate, trend, weight));
                    count++;
                    logger.info("HotSpot News Entries : " + count);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        logger.info("News List count " + TextClassificationStore.getInstance().getNewsList().size());
    }

    private void countNewsWords(String exchange, String symbol, String date, int trend, int weight) {
        Connection dbConnection = sqlConnector.connect();

            if (dbConnection != null) {
                PreparedStatement statement = null;

//                String createTableSQL = "select heading, body from msc2.news where str_to_date(NEWS_DATE, '%m/%d/%Y') = ? and exchange = ? and symbol = ?";
                String createTableSQL = "select heading, body, news_id from msc.news where NEWS_DATE = ? and exchange" +
                        " = ? and symbol = ?";
                try {
                    statement = dbConnection.prepareStatement(createTableSQL);
                    statement.setString(1, date);
                    statement.setString(2, exchange);
                    statement.setString(3, symbol);
                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        String heading = rs.getString(1).toLowerCase();
                        String body = rs.getString(2).toLowerCase();
                        int newsId = rs.getInt(3);
                        NewsData newsData = new NewsData(""+newsId, exchange, symbol, date, trend, weight);
                        newsData.setDataModelInput(String.join(" ", TextUtils.parseSentences(ExudeData.getInstance()
                                .filterStoppingsKeepDuplicates(heading))));
                        newsData.setNewsHeading(heading);
                        newsData.setNewsBody(body);
                        TextClassificationStore.getInstance().getNewsList().add(newsData);
                        logger.info("News loaded: " + newsId + " : " + exchange + " : " + symbol + " : " + weight + "" +
                                " :" + trend);
                        updateTextClassifier(newsData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            }
    }

    private void updateTextClassifier(NewsData newsData) {
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

    private void dumpNews(){
        TextClassificationStore.getInstance().dumpNews();
    }
}

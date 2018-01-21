package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.NewsData;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.ucsc.mcs.impl.tfidf.connector.WeightedDocument;
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by JagathA on 1/20/2018.
 */
public class TfIdfNewsClassifier {

    private SqlConnector sqlConnector = null;

    private List<TfIdfNewsData> tfIdfNewsDataArrayList = new ArrayList<>();

    public TfIdfNewsClassifier(SqlConnector sqlConnector){
        this.sqlConnector = sqlConnector;
    }

    public void classifyNews() {
//        this.loadNews();
//        this.countNewsWords();
//        this.dumpNews();
        this.calculateTermWeights();
//        this.calculateTfIdfValues();
    }

    private void calculateTermWeights(){
        for(NewsData newsData : TextClassificationStore.getInstance().loadNewsFromFile()){
            try {

                List<String> document = TextUtils.parseSentences(ExudeData
                        .getInstance().filterStoppingsKeepDuplicates(newsData.getNewsHeading()), true);
//                System.out.println("Original : " + document.toString());
//                System.out.println("Updated : " + TextUtils.removeDuplicates(document));
                TextClassificationStore.getWeightedDocumentList().add(new WeightedDocument(document, newsData.getWeight()));
//                tfIdfNewsDataArrayList.add(new TfIdfNewsData(newsData, document));
            } catch (InvalidDataException e) {
                System.out.println("Error data News : " + newsData.getNewsHeading());
            }
        }

        System.out.println("+++++++++++ News Docs Size " + TextClassificationStore.getWeightedDocumentList().size());
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
                    TextClassificationStore.getInstance().getNewsList().add(new NewsData(exchange, symbol, spotDate, trend, weight));
                    count++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        System.out.println("News List count " + TextClassificationStore.getInstance().getNewsList().size());
    }

    private void countNewsWords() {
        Connection dbConnection = sqlConnector.connect();

        for (NewsData newsData : TextClassificationStore.getInstance().getNewsList()) {
            if (dbConnection != null) {
                PreparedStatement statement = null;

//                String createTableSQL = "select heading, body from msc.news where str_to_date(NEWS_DATE, '%m/%d/%Y') = ? and exchange = ? and symbol = ?";
                String createTableSQL = "select heading, body from msc.news where NEWS_DATE = ? and exchange = ? and symbol = ?";
                try {
                    statement = dbConnection.prepareStatement(createTableSQL);
                    statement.setString(1, newsData.getNewsDate());
                    statement.setString(2, newsData.getExchange());
                    statement.setString(3, newsData.getSymbol());
                    ResultSet rs = statement.executeQuery();

                    while (rs.next()) {
                        String heading = rs.getString(1).toLowerCase();
                        String body = rs.getString(2).toLowerCase();
                        newsData.setDataModelInput(String.join(" ", TextUtils.parseSentences(ExudeData.getInstance()
                                .filterStoppingsKeepDuplicates(heading))));
                        newsData.setNewsHeading(heading);
                        newsData.setNewsBody(body);
                        updateTextClassifier(newsData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
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

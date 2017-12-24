package com.ucsc.mcs.impl.datamanage;

import com.ucsc.mcs.impl.connector.SqlConnector;
import com.ucsc.mcs.impl.data.NewsData;
import com.ucsc.mcs.impl.data.TextEntry;
import com.ucsc.mcs.impl.classifier.TextClassificationStore;
import com.ucsc.mcs.impl.utils.TextUtils;
import com.uttesh.exude.ExudeData;

import javax.xml.soap.Text;
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

    public NewsClassifier(SqlConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    public void classifyNews() {
        loadNews();
        countNewsWords();
    }

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
                    System.out.println("News " + exchange + " : " + symbol + " : " + spotDate + " : " + trend + " : " + weight);
                    TextClassificationStore.getInstance().getNewsList().add(new NewsData(exchange, symbol, spotDate, trend, weight));
                    count++;

                    if (count == 4) {
                        break;
                    }
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
                        System.out.println("News details : " + newsData.getNewsDate() + " " + newsData.getTrend() + " " + newsData.getWeight() + " " + heading + " : " + body);
                        newsData.setNewsHeading(String.join(" ", TextUtils.parseSentences(ExudeData.getInstance().filterStoppingsKeepDuplicates(heading + " " + body))));
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
        String[] words = newsData.getNewsHeading().split(" ");
        String refactoredWord = "";

        for (String word : words) {
            refactoredWord = word.toLowerCase().replaceAll("[0-9]", "");
            if (refactoredWord.length() > 1) {
                TextEntry textEntry = new TextEntry(refactoredWord.toLowerCase());
                textEntry.setScore(newsData.getWeight() * newsData.getTrend());
                TextClassificationStore.getInstance().addTextClassificationForText(refactoredWord.toLowerCase(), textEntry);
            } else {
                System.out.println("Numerical or shorter  word found - ignoring : " + word);
            }
        }
    }
}

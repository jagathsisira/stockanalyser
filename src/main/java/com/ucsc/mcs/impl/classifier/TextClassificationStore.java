package com.ucsc.mcs.impl.classifier;

import com.ucsc.mcs.impl.connector.SqlConnector;
import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.data.NewsData;
import com.ucsc.mcs.impl.data.TextEntry;
import com.ucsc.mcs.impl.utils.TextUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by JagathA on 11/24/2017.
 */
public class TextClassificationStore {

    private static TextClassificationStore textClassificationStore = new TextClassificationStore();

    private HashMap<String, ArrayList<TextEntry>> textClassifiers = new HashMap<String, ArrayList<TextEntry>>();

    private ArrayList<NewsData> newsList = new ArrayList<>();

    private ArrayList<AnnouncementData> announcementsList = new ArrayList<>();

    private ArrayList<String> positiveWords = new ArrayList<>();

    private ArrayList<String> negativeWords = new ArrayList<>();

    public static TextClassificationStore getInstance(){
        return textClassificationStore;
    }

    public ArrayList<TextEntry> getTextClassificationsForText(String text){
        return this.textClassifiers.get(text);
    }

    public void addTextClassificationForText(String text, TextEntry textEntry) {
        ArrayList<TextEntry> textList = this.textClassifiers.get(text);
        if (textList == null) {
            textList = new ArrayList<TextEntry>();
            this.textClassifiers.put(text, textList);
        }

        textList.add(textEntry);
    }

    public HashMap<String, ArrayList<TextEntry>> getTextClassifiers() {
        return textClassifiers;
    }

    public void printStoreStats(){
        Set<String> keys = textClassifiers.keySet();

        for(String key: keys){
            ArrayList<TextEntry> entries = textClassifiers.get(key);

            for(TextEntry textEntry: entries){
                System.out.println("+++ " + textEntry.getText() + ":" + textEntry.getScore());
            }
        }
    }

    public void updateClassifierDatabase(SqlConnector sqlConnector){
        Connection dbConnection = sqlConnector.connect();
        Set<String> keys = textClassifiers.keySet();

        for(String key: keys){
            ArrayList<TextEntry> entries = textClassifiers.get(key);
            int totalCountPositive = 0;
            int totalCountNegative = 0;
            double totalWeightPositive = 0;
            double totalWeightNegative = 0;
            double totalPositiveAvg = 0;
            double totalNegativeAvg = 0;

            for(TextEntry textEntry: entries){
                if(textEntry.getScore() > 0) {
                    totalCountPositive ++;
                    totalWeightPositive = totalWeightPositive + textEntry.getScore();
                } else {
                    totalCountNegative ++;
                    totalWeightNegative = totalWeightNegative + textEntry.getScore();
                }
            }

            if(totalCountNegative > 0){
                totalNegativeAvg = totalWeightNegative/totalCountNegative;
            }

            if(totalCountPositive > 0){
                totalPositiveAvg = totalWeightPositive/totalCountPositive;
            }


            System.out.println("----- " + key + " " + (totalCountNegative + totalCountPositive) + " "
                    + totalPositiveAvg + " " + totalNegativeAvg);

            if (dbConnection != null) {
                PreparedStatement statement = null;

                String createTableSQL = "insert into msc.classifier values (?,?,?)";
                try {
                    statement = dbConnection.prepareStatement(createTableSQL);
                    statement.setString(1, key);
                    statement.setInt(2, (totalCountNegative + totalCountPositive));
                    System.out.println(">>> " +totalCountPositive + " " + totalCountNegative + " " + totalWeightPositive + " " + totalWeightNegative);
                    statement.setDouble(3, (totalPositiveAvg + totalNegativeAvg));

                    statement.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                }
            }

            if((totalPositiveAvg + totalNegativeAvg) > 0){
                positiveWords.add(key);
            } else {
                negativeWords.add(key);
            }
        }
    }

    public void updateClassifierTextFile(){
        try {
            TextUtils.writeToFile("./src/main/resources/positive.txt", positiveWords);
            TextUtils.writeToFile("./src/main/resources/negative.txt", negativeWords);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<NewsData> getNewsList() {
        return newsList;
    }

    public ArrayList<AnnouncementData> getAnnouncementsList() {
        return announcementsList;
    }
}


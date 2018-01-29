package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.data.NewsData;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.ucsc.mcs.impl.tfidf.connector.WeightedDocument;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by JagathA on 11/24/2017.
 */
public class TextClassificationStore {
    final static Logger logger = Logger.getLogger(TextClassificationStore.class);

    private static TextClassificationStore textClassificationStore = new TextClassificationStore();

    private HashMap<String, ArrayList<TextEntry>> textClassifiers = new HashMap<String, ArrayList<TextEntry>>();

    private ArrayList<NewsData> newsList = new ArrayList<>();

    private ArrayList<AnnouncementData> announcementsList = new ArrayList<>();

    private ArrayList<String> positiveWords = new ArrayList<>();

    private ArrayList<String> negativeWords = new ArrayList<>();

    private static SnowballStemmer snowballStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

    private static List<WeightedDocument> weightedDocumentList = new ArrayList<>();

    private static List<String> wordMaster = new ArrayList<>();

    public static TextClassificationStore getInstance(){
        return textClassificationStore;
    }

    public ArrayList<TextEntry> getTextClassificationsForText(String text){
        return this.textClassifiers.get(text);
    }

    public void addTextClassificationForText(String text, TextEntry textEntry) {
        String stemmedText = (String)snowballStemmer.stem(text);
        if(stemmedText.length() > 1) {
            ArrayList<TextEntry> textList = this.textClassifiers.get(stemmedText);
            if (textList == null) {
                textList = new ArrayList<>();
                this.textClassifiers.put(stemmedText, textList);
            }

            textList.add(textEntry);
        }
    }

    public void addTextClassificationForText(String text, TextEntry textEntry, boolean isStem) {

        if(isStem){
            addTextClassificationForText(text, textEntry);
        } else {
            if (text.length() > 1) {
                ArrayList<TextEntry> textList = this.textClassifiers.get(text);
                if (textList == null) {
                    textList = new ArrayList<>();
                    this.textClassifiers.put(text, textList);
                }
                textList.add(textEntry);
            }
        }
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

    public void updateClassifierDatabaseTfIdf(SqlConnector sqlConnector){
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
                if(textEntry.getScore() >= 0) {
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


            if (dbConnection != null) {
                PreparedStatement statement = null;

                String createTableSQL = "insert into msc.classifier_tf_idf values (?,?,?)";
                try {
                    statement = dbConnection.prepareStatement(createTableSQL);
                    statement.setString(1, key);
                    statement.setInt(2, (totalCountNegative + totalCountPositive));
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
                if(textEntry.getScore() >= 0) {
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

            if (dbConnection != null) {
                PreparedStatement statement = null;

                String createTableSQL = "insert into msc.classifier values (?,?,?)";
                try {
                    statement = dbConnection.prepareStatement(createTableSQL);
                    statement.setString(1, key);
                    statement.setInt(2, (totalCountNegative + totalCountPositive));
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

    public void updateClassifierTextFileTfIdf(){
        try {
            TextUtils.writeToFile("./src/main/resources/positiveTfIdf.txt", positiveWords);
            TextUtils.writeToFile("./src/main/resources/negativeTfIdf.txt", negativeWords);
        } catch (IOException e) {
            e.printStackTrace();
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

    public void dumpAnnouncements(){
        try{
            FileOutputStream fos= new FileOutputStream("AnnouncementsList.dat");
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(announcementsList);
            oos.close();
            fos.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public void dumpNews(){
        try{
            FileOutputStream fos= new FileOutputStream("NewsList.dat");
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(newsList);
            oos.close();
            fos.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public ArrayList<AnnouncementData> loadAnnouncementsFromFile(){
        ArrayList<AnnouncementData> announcementData = new ArrayList<>();
        try
        {
            FileInputStream fis = new FileInputStream("AnnouncementsList.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            announcementData = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
        }catch(Exception ioe){
            ioe.printStackTrace();
        }

        return announcementData;
    }

    public ArrayList<NewsData> loadNewsFromFile(){
        ArrayList<NewsData> newsData = new ArrayList<>();
        try
        {
            FileInputStream fis = new FileInputStream("NewsList.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            newsData = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
        }catch(Exception ioe){
            ioe.printStackTrace();
        }
        logger.info("News Data List Size : " + newsData.size());
        return newsData;
    }

    public static List<WeightedDocument> getWeightedDocumentList() {
        return weightedDocumentList;
    }

    public static void generateWordMaster(){
        List<String> localWordMaster = new ArrayList<>();
        loadWeightedDocsFromFile();
        //populate list
        for(WeightedDocument document: weightedDocumentList){
            for(String word : document.getDocument()){
                if(!localWordMaster.contains(word)){
                    localWordMaster.add(word);
                }
            }
        }

        logger.info("Local Word Master Completed with size : " + localWordMaster.size());
        logger.info("Local Word Master:  " + localWordMaster.toString());

        Collections.sort(localWordMaster, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        logger.info("Ordered Local Word Master Completed with size : " + localWordMaster.size());
        logger.info("Ordered Local Word Master:  " + localWordMaster.toString());

        List<List<String>> tfIfdDocList = new ArrayList<>();
        for(WeightedDocument weightedDocument : weightedDocumentList){
            tfIfdDocList.add(weightedDocument.getDocument());
        }

        double idfTotal = 0;
        double idfAvg = 0;
        int idfCount = 0;
        for(String word : localWordMaster){
            double tfIdf = TFIDFCalculator.getInstance().idf(tfIfdDocList, word);
            idfCount ++;
            idfTotal = idfTotal + tfIdf;
            idfAvg = (idfTotal/idfCount);
            logger.info(">>> " + tfIdf + " Count: " + idfCount + " Avg: " + idfAvg + " : " + word);

            if(tfIdf < 10){
                wordMaster.add(word);
            }
        }

        logger.info("TFIDF Word Master Completed with size : " + wordMaster.size());
        logger.info("TFIDF Word Master:  " + wordMaster.toString());

        dumpWordMaster();
    }

    public static void dumpWordMaster(){
        try{
            FileOutputStream fos= new FileOutputStream("wordMaster.dat");
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(wordMaster);
            oos.close();
            fos.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public List<String> loadWordMasterFile(){
        List<String> wordMaster = new ArrayList<>();
        try
        {
            FileInputStream fis = new FileInputStream("wordMaster.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            wordMaster = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
        }catch(Exception ioe){
            ioe.printStackTrace();
        }

        return wordMaster;
    }

    public static List<String> getWordMaster() {
        return wordMaster;
    }

    public static void setWordMaster(List<String> wordMaster) {
        TextClassificationStore.wordMaster = wordMaster;
    }

    public static void dumpWeightedDocs(){
        try{
            FileOutputStream fos= new FileOutputStream("weightedDocs.dat");
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(weightedDocumentList);
            oos.close();
            fos.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public static List<WeightedDocument> loadWeightedDocsFromFile(){
        List<WeightedDocument> weightedDocs = new ArrayList<>();
        try
        {
            FileInputStream fis = new FileInputStream("weightedDocs.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            weightedDocumentList = (ArrayList) ois.readObject();
            ois.close();
            fis.close();
        }catch(Exception ioe){
            ioe.printStackTrace();
        }

        logger.info("Weighted Doc list loaded : " + weightedDocumentList.size());
        return weightedDocs;
    }
}


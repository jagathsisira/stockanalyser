package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.data.NewsData;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JagathA on 12/24/2017.
 */
public class NaiveBayesClassifier {

    private static final NaiveBayesClassifier naiveBayesClassification = new NaiveBayesClassifier();

    private ArrayList<String> positiveWords = new ArrayList<>();
    private ArrayList<String> negativeWords = new ArrayList<>();

    private NaiveBayesClassifier() {
    }

    public static NaiveBayesClassifier getInstance() {
        return naiveBayesClassification;
    }

    public void predict(SqlConnector connector) {
        // Two examples to learn from.
        ArrayList<Boolean> results = new ArrayList<>();

        trainClassifier(connector);

        // Create a new bayes classifier with string categories and string features.
        Classifier<String, String> bayes = new BayesClassifier<String, String>();

        // Learn by classifying examples.
        // New categories can be added on the fly, when they are first used.
        // A classification consists of a category and a list of features
        // that resulted in the classification in that category.

        bayes.learn("positive", positiveWords);
        bayes.learn("negative", negativeWords);

        int count = 0;
        int totalCount = 0;
        int correctCount = 0;
        SnowballStemmer snowballStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

        for(NewsData newsData : TextClassificationStore.getInstance().loadNewsFromFile()) {
            //Classify a single sentence
            try {
                String sentence = newsData.getNewsHeading();
        System.out.println("Hading: " + sentence);
        System.out.println(">>> " + String.join(" ", TextUtils.parseSentences(ExudeData
                .getInstance().filterStoppingsKeepDuplicates(sentence))));
                String originalTrend = newsData.getTrend() > 0 ? "positive" : "negative";
                List<String> wordList = TextUtils.parseSentences(ExudeData.getInstance()
                        .filterStoppingsKeepDuplicates
                                (sentence));
                List<String> updatedList = new ArrayList<>();
                for (int i=0;i < wordList.size(); i++) {
                    String word = wordList.get(i).toLowerCase().replaceAll("[0-9]", "");
                    String refactored = (String)(snowballStemmer.stem(word));
                    if(refactored.length() > 1) {
                        updatedList.add(refactored);
                        System.out.println(">>>> " + refactored);
                    }
                }

                String predictedTrend = bayes.classify(updatedList).getCategory();
                totalCount ++;
                count ++;
                if (originalTrend.equalsIgnoreCase(predictedTrend)){
                    correctCount ++;
                }

//                System.out.println( "Original : " + originalTrend + " Predicted : " + predictedTrend + " : " +
//                        ((correctCount * 100)/totalCount));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(count > 1000){
                break;
            }

        }

        count = 0;

        for(AnnouncementData announcementData : TextClassificationStore.getInstance().loadAnnouncementsFromFile()) {
            //Classify a single sentence
            try {
                String sentence = announcementData.getAnnHeading();
                String originalTrend = announcementData.getTrend() > 0 ? "positive" : "negative";
                List<String> wordList = TextUtils.parseSentences(ExudeData.getInstance()
                        .filterStoppingsKeepDuplicates
                                (sentence));
                List<String> updatedList = new ArrayList<>();
                for (int i=0;i < wordList.size(); i++) {
                    String word = wordList.get(i).toLowerCase().replaceAll("[0-9]", "");
                    String refactored = (String)(snowballStemmer.stem(word));
                    if(refactored.length() > 1) {
                        updatedList.add(refactored);
                        System.out.println(">>>><<<< " + refactored);
                    }
                }
                String predictedTrend = bayes.classify(updatedList).getCategory();
                totalCount ++;
                count ++;
                if (originalTrend.equalsIgnoreCase(predictedTrend)){
                    correctCount ++;
                }

//                System.out.println( "Original : " + originalTrend + " Predicted : " + predictedTrend + " : " +
//                        ((correctCount * 100)/totalCount));
                if(count > 1000){
                    break;
                }
            } catch (InvalidDataException e) {
                e.printStackTrace();
            }
        }

        System.out.println("========> Naiver Bayes - Total news used : " + totalCount + " : " + (correctCount * 100)/totalCount);

        bayes.setMemoryCapacity(500);
    }

    private void trainClassifier(SqlConnector sqlConnector){
        Connection dbConnection = sqlConnector.connect();

        double avgCount = getAvgCount(sqlConnector);

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select word, avg_weight from msc.classifier where count > ?";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                statement.setInt(1,(int)avgCount);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String word = rs.getString(1);
                    double weight = rs.getDouble(2);
                    if(weight < 0){
                        negativeWords.add(word);
                    } else {
                        positiveWords.add(word);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    private double getAvgCount(SqlConnector sqlConnector) {
        Connection dbConnection = sqlConnector.connect();
        double avgCount = 0;

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "SELECT AVG(count) FROM msc.classifier";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    avgCount = rs.getDouble(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }
        System.out.println("avgCount " + avgCount + " : " + new Double(avgCount).intValue());
        return avgCount;
    }
}

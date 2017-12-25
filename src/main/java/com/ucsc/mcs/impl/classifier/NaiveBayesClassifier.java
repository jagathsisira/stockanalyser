package com.ucsc.mcs.impl.classifier;

import com.ucsc.mcs.impl.connector.SqlConnector;
import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.data.NewsData;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

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
        String positiveText = null;
        String negativeText = null;
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


        for(NewsData newsData : TextClassificationStore.getInstance().getNewsList()) {
            //Classify a single sentence
            String sentence = newsData.getNewsHeading();
            String originalTrend = newsData.getTrend() > 0 ? "positive" : "negative";
            String predictedTrend = bayes.classify(Arrays.asList(sentence.split(" "))).getCategory();
            System.out.println( "Original : " + originalTrend + " Predicted : " + predictedTrend);
            results.add(originalTrend.equalsIgnoreCase(predictedTrend));
        }

        for(AnnouncementData announcementData : TextClassificationStore.getInstance().getAnnouncementsList()) {
            //Classify a single sentence
            String sentence = announcementData.getAnnHeading();
            String originalTrend = announcementData.getTrend() > 0 ? "positive" : "negative";
            String predictedTrend = bayes.classify(Arrays.asList(sentence.split(" "))).getCategory();
            System.out.println( "Original : " + originalTrend + " Predicted : " + predictedTrend);
            results.add(originalTrend.equalsIgnoreCase(predictedTrend));
        }

        int correctCount = 0;
        int totalCount = 0;

        for(Boolean result: results){
            totalCount ++;
            if(result){
                correctCount ++;
            }
        }

        System.out.println("=====>>> " + totalCount + " " + correctCount + " " + (correctCount*100/totalCount));

        bayes.setMemoryCapacity(500);
    }

    private void trainClassifier(SqlConnector sqlConnector){
        Connection dbConnection = sqlConnector.connect();
        int count = 0;

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select word, avg_weight from msc.classifier";
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String word = rs.getString(1);
                    double weight = rs.getDouble(2);
                    System.out.println("word " + word + " : " + weight);
                    if(weight < 0){
                        negativeWords.add(word);
                    } else {
                        positiveWords.add(word);
                    }
                    count ++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        System.out.println("+++++++++++ Words loaded " + positiveWords.size() + " " + negativeWords.size());

    }
}

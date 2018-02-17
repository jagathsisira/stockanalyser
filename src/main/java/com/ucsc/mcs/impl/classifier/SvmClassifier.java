package com.ucsc.mcs.impl.classifier;

import com.ucsc.mcs.impl.tfidf.TextClassificationStore;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.data.NewsData;
import com.ucsc.mcs.impl.tfidf.TextUtils;
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import libsvm.LibSVM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by JagathA on 12/25/2017.
 */
public class SvmClassifier {

    private static final SvmClassifier svmClassifier = new SvmClassifier();

    private static final SnowballStemmer snowballStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

    private Map<String, ArrayList<String>> weightMap = new HashMap<>();

    private ArrayList<String> globalWordsList = new ArrayList<>();

    private SvmClassifier() {
    }

    public static SvmClassifier getInstance() {
        return svmClassifier;
    }

    public void predict(SqlConnector connector) {
        ArrayList<Boolean> results = new ArrayList<>();

        this.trainClassifier(connector);
        int correctCount = 0;
        int totalCount = 0;

        try {
        /* Load a data set */
            Dataset data = FileHandler.loadDataset(new File("./src/main/resources/svm.data"), 0, ",");
        /*
         * Contruct a LibSVM classifier with default settings.
         */System.out.println("Start building classifier");
            net.sf.javaml.classification.Classifier svm = new LibSVM();
            svm.buildClassifier(data);
            System.out.println("Finished building classifier");

        /*
         * Load a data set, this can be a different one, but we will use the
         * same one.
         */

            int count = 0;
            for (NewsData newsData : TextClassificationStore.getInstance().loadNewsFromFile()) {
                try {
                    //Classify a single sentence
                    String sentence = newsData.getNewsHeading();
                    String originalTrend = newsData.getTrend() > 0 ? "positive" : "negative";

                    StringBuilder stringBuilder = new StringBuilder();

                    List<String> wordList = TextUtils.parseSentences(ExudeData.getInstance()
                            .filterStoppingsKeepDuplicates
                            (sentence));
                    for (String word : wordList) {
                        String refactored = (String)(snowballStemmer.stem(word.toLowerCase().replaceAll("[0-9]", "")));
                        if(refactored.length() > 1) {
                            stringBuilder.append(refactored.hashCode());
                            stringBuilder.append(",");
                        }
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
//                    stringBuilder.append("\n");
//                    System.out.println("SVM Predict Data : " + stringBuilder.toString());

                    this.writeToFile("./src/main/resources/tmpnews.data", stringBuilder.toString());

                    Dataset dataForClassification = FileHandler.loadDataset(new File("./src/main/resources/tmpnews.data"), ",");
        /* Counters for correct and wrong predictions. */
                    int correct = 0, wrong = 0;
        /* Classify all instances and check with the correct class values */

                    for (Instance inst : dataForClassification) {
                        Object predictedClassValue = svm.classify(inst);
//                        System.out.println("Predicted: " + count + " " +predictedClassValue);
                        System.out.println("Original : " + count + " " +originalTrend + " Predicted : " + ((Double.parseDouble((String) predictedClassValue)) > 0 ? "positive" : "negative"));
                        if(originalTrend.equalsIgnoreCase(((Double.parseDouble((String) predictedClassValue)) > 0 ?
                                "positive" : "negative"))){
                            correctCount ++;
                        }
                        totalCount ++;
                    }
                    count ++;
                } catch (InvalidDataException e) {
//                    e.printStackTrace();
                }
//                if(count > 500){
//                    break;
//                }
            }

            System.out.println("News Count : " + count);

            count = 0;
            for (AnnouncementData announcementData : TextClassificationStore.getInstance().loadAnnouncementsFromFile()) {
                try {
                    //Classify a single sentence
                    String sentence = announcementData.getAnnHeading();// + announcementData.getAnnBody();

//                    System.out.println(">>> " +sentence);
                    String originalTrend = announcementData.getTrend() > 0 ? "positive" : "negative";

                    StringBuilder stringBuilder = new StringBuilder();

                    List<String> wordList = TextUtils.parseSentences(ExudeData.getInstance()
                            .filterStoppingsKeepDuplicates
                                    (sentence));
                    for (String word : wordList) {
                        String refactored = (String)(snowballStemmer.stem(word.toLowerCase().replaceAll("[0-9]", "")));
                        if(refactored.length() > 1 && globalWordsList.contains(refactored)) {
//                            System.out.println("+++ word found : " + refactored );
                            stringBuilder.append(refactored.hashCode());
                            stringBuilder.append(",");
                        }
                    }

                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
//                stringBuilder.append("\n");
//                System.out.println("SVM Predict Data: " + stringBuilder.toString());

                    this.writeToFile("./src/main/resources/tmpanns.data", stringBuilder.toString());

                    Dataset dataForClassification = FileHandler.loadDataset(new File("./src/main/resources/tmpanns.data"), ",");
        /* Counters for correct and wrong predictions. */
                    int correct = 0, wrong = 0;
        /* Classify all instances and check with the correct class values */
                    for (Instance inst : dataForClassification) {
                        Object predictedClassValue = svm.classify(inst);
//                        System.out.println("Predicted: " + predictedClassValue + " : " + originalTrend);
                        System.out.println("Original : " + originalTrend + " Predicted : " + ((Double.parseDouble((String) predictedClassValue)) > 0 ? "positive" : "negative"));
                        if (originalTrend.equalsIgnoreCase(((Double.parseDouble((String) predictedClassValue)) > 0 ?
                                "positive" : "negative"))) {
                            correctCount++;
                        }
                        totalCount++;
                    }
                    count ++;
//                System.out.println("Success pct : " + (correctCount * 100)/totalCount);
                } catch (InvalidDataException e) {
//                    e.printStackTrace();
                }
//                if(count > 500){
//                    break;
//                }
            }
            System.out.println("Announcement Count : " + count);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("========> SVM Total news used : " + totalCount + " : " + (correctCount * 100)/totalCount);
    }

    private void trainClassifier(SqlConnector sqlConnector) {
        Connection dbConnection = sqlConnector.connect();

        double avgCount = getAvgCount(sqlConnector);

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "select word, avg_weight, count from msc2.classifier";// where count > ?
            try {
                statement = dbConnection.prepareStatement(createTableSQL);
//                statement.setInt(1, new Double(avgCount).intValue());
                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    String word = rs.getString(1);
                    double weight = rs.getDouble(2);
                    int count = rs.getInt(3);
//                    double updatedWeight = Math.round(weight);
                    double updatedWeight = (int)(Math.round( (getUpdatedWeight(weight) * count) / 100.0) * 100);
//                    double updatedWeight = (int)(Math.round( (getUpdatedWeight(weight)) / 100.0) * 100);

                    ArrayList<String> wordList = weightMap.get("" + updatedWeight);
                    if (wordList == null) {
                        wordList = new ArrayList<>();
                        weightMap.put("" + updatedWeight, wordList);

                    }
                    wordList.add(word);

                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        StringBuilder stringBuilder = new StringBuilder();

        int count = 0;
        for (String weight : weightMap.keySet()) {
            ArrayList<String> wordList = weightMap.get(weight);
            System.out.println(">>> " + weight + " " + wordList.size());
            stringBuilder.append(weight);
            for (String word : wordList) {
                globalWordsList.add(word);
                int hashedWord = (snowballStemmer.stem(word)).hashCode();
                stringBuilder.append(",");
                stringBuilder.append(hashedWord);
            }
            stringBuilder.append("\n");
//            if(count ++ > 200){
//                break;
//            }
        }

        System.out.println("SVM Train Data: Completed " + weightMap.size());

        this.writeToFile("./src/main/resources/svm.data", stringBuilder.toString());


    }

    private void writeToFile(String path, String data) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(path);
            bw = new BufferedWriter(fw);
            bw.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private double getAvgCount(SqlConnector sqlConnector) {
        Connection dbConnection = sqlConnector.connect();
        double avgCount = 0;

        if (dbConnection != null) {
            PreparedStatement statement = null;

            String createTableSQL = "SELECT AVG(count) FROM msc2.classifier";
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
        return avgCount * 1.5;
    }

    private double getUpdatedWeight(double weight){
        if(weight >= 0) {
            return Math.ceil(weight);
        } else {
            return -1 * Math.ceil(Math.abs(weight));
        }
    }
}

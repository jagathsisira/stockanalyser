package com.ucsc.mcs.impl.classifier;

import com.ucsc.mcs.impl.connector.SqlConnector;
import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.data.NewsData;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import libsvm.LibSVM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;
import ucar.ma2.ArrayLong;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JagathA on 12/25/2017.
 */
public class SvmClassifier {

    private static final SvmClassifier svmClassifier = new SvmClassifier();

    private Map<String, ArrayList<String>> weightMap = new HashMap<>();

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
         */
            net.sf.javaml.classification.Classifier svm = new LibSVM();
            svm.buildClassifier(data);

        /*
         * Load a data set, this can be a different one, but we will use the
         * same one.
         */

            for (NewsData newsData : TextClassificationStore.getInstance().getNewsList()) {
                //Classify a single sentence
                String sentence = newsData.getNewsHeading();
                String originalTrend = newsData.getTrend() > 0 ? "positive" : "negative";

                StringBuilder stringBuilder = new StringBuilder();

                for (String word : sentence.split(" ")) {
                    stringBuilder.append(word.hashCode());
                    stringBuilder.append(",");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.append("\n");
                System.out.println("SVM Predict Data: " + stringBuilder.toString());

                this.writeToFile("./src/main/resources/tmpnews.data", stringBuilder.toString());

                Dataset dataForClassification = FileHandler.loadDataset(new File("./src/main/resources/tmpnews.data"), ",");
        /* Counters for correct and wrong predictions. */
                int correct = 0, wrong = 0;
        /* Classify all instances and check with the correct class values */
                for (Instance inst : dataForClassification) {
                    Object predictedClassValue = svm.classify(inst);
                    System.out.println("Predicted: " + predictedClassValue);
                    System.out.println("Original : " + originalTrend + " Predicted : " + ((Double.parseDouble((String) predictedClassValue)) > 0 ? "positive" : "negative"));
                    results.add(originalTrend.equalsIgnoreCase(((Double.parseDouble((String) predictedClassValue)) > 0 ? "positive" : "negative")));
                }
            }


            for (AnnouncementData announcementData : TextClassificationStore.getInstance().getAnnouncementsList()) {
                //Classify a single sentence
                String sentence = announcementData.getAnnHeading();
                String originalTrend = announcementData.getTrend() > 0 ? "positive" : "negative";

                StringBuilder stringBuilder = new StringBuilder();

                for (String word : sentence.split(" ")) {
                    stringBuilder.append(word.hashCode());
                    stringBuilder.append(",");
                }

                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                stringBuilder.append("\n");
                System.out.println("SVM Predict Data: " + stringBuilder.toString());

                this.writeToFile("./src/main/resources/tmpanns.data", stringBuilder.toString());

                Dataset dataForClassification = FileHandler.loadDataset(new File("./src/main/resources/tmpanns.data"), ",");
        /* Counters for correct and wrong predictions. */
                int correct = 0, wrong = 0;
        /* Classify all instances and check with the correct class values */
                for (Instance inst : dataForClassification) {
                    Object predictedClassValue = svm.classify(inst);
                    System.out.println("Predicted: " + predictedClassValue);
                    System.out.println("Original : " + originalTrend + " Predicted : " + ((Double.parseDouble((String) predictedClassValue)) > 0 ? "positive" : "negative"));
                    results.add(originalTrend.equalsIgnoreCase(((Double.parseDouble((String) predictedClassValue)) > 0 ? "positive" : "negative")));
                }
            }

            correctCount = 0;
            totalCount = 0;

            for (Boolean result : results) {
                totalCount++;
                if (result) {
                    correctCount++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("=====>>> " + totalCount + " " + correctCount + " " + (correctCount * 100 / totalCount));

    }

    private void trainClassifier(SqlConnector sqlConnector) {
        Connection dbConnection = sqlConnector.connect();

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

                    ArrayList<String> wordList = weightMap.get("" + weight);
                    if (wordList == null) {
                        wordList = new ArrayList<>();
                        weightMap.put("" + weight, wordList);
                    }
                    wordList.add(word);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
            }
        }

        System.out.println("+++++++++++ Weight and Words loaded " + weightMap.size());

        StringBuilder stringBuilder = new StringBuilder();

        for (String weight : weightMap.keySet()) {
            ArrayList<String> wordList = weightMap.get(weight);
            stringBuilder.append(weight);
            for (String word : wordList) {
                stringBuilder.append(",");
                stringBuilder.append(word.hashCode());
            }
            stringBuilder.append("\n");
        }

        System.out.println("SVM Train Data: " + stringBuilder.toString());

        this.writeToFile("./src/main/resources/svm.data", stringBuilder.toString());


    }

    private void writeToFile(String path, String data) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(path);
            bw = new BufferedWriter(fw);
            bw.write(data);
            System.out.println("Done");
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
}

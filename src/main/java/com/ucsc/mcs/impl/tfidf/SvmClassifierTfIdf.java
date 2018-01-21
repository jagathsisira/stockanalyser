package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.NewsData;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.ucsc.mcs.impl.tfidf.connector.WeightedDocument;
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import libsvm.LibSVM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by JagathA on 12/25/2017.
 */
public class SvmClassifierTfIdf {

    private static final SvmClassifierTfIdf svmClassifierTfIdf = new SvmClassifierTfIdf();

    private static final SnowballStemmer snowballStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

    private Map<String, ArrayList<String>> weightMap = new HashMap<>();

    private ArrayList<String> globalWordsList = new ArrayList<>();

    private SvmClassifierTfIdf() {
    }

    public static SvmClassifierTfIdf getInstance() {
        return svmClassifierTfIdf;
    }

    public void predict(SqlConnector connector) {
        ArrayList<Boolean> results = new ArrayList<>();

        this.trainClassifier(connector);
        int correctCount = 0;
        int totalCount = 0;

        try {
        /* Load a data set */
            Dataset data = FileHandler.loadDataset(new File("./src/main/resources/svmTfIdf.data"), 0, ",");
        /*
         * Contruct a LibSVM classifier with default settings.
         *
         */
            long startTime = System.currentTimeMillis();
            System.out.println("Start building classifier " + startTime);

            net.sf.javaml.classification.Classifier svm = new LibSVM();
            svm.buildClassifier(data);
            dumpSvmClassifier(svm);
//            net.sf.javaml.classification.Classifier svm =loadSvmClassifier();
            long endTime = System.currentTimeMillis();
            System.out.println("Finished building classifier " + endTime + " in " + ((endTime - startTime)/1000) + " " +
                    "seconds");

        /*
         * Load a data set, this can be a different one, but we will use the
         * same one.
         */

            int count = 0;
            for (NewsData newsData : TextClassificationStore.getInstance().loadNewsFromFile()) {
                try {
                    //Classify a single sentence
                    String sentence = newsData.getNewsHeading();
                    int originalWeight = newsData.getWeight();

                    StringBuilder stringBuilder = new StringBuilder();

                    List<String> document = TextUtils.parseSentences(ExudeData
                            .getInstance().filterStoppingsKeepDuplicates(sentence), true);
                    List<String> wordMaster = TextClassificationStore.getInstance().loadWordMasterFile();
                    for(String masterWord: wordMaster) {
                        if(document.contains(masterWord)) {
                            stringBuilder.append("1");
                            stringBuilder.append(",");
                        } else {
                            stringBuilder.append("0");
                            stringBuilder.append(",");
                        }
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);

                    this.writeToFile("./src/main/resources/tmpNewsTfIds.data", stringBuilder.toString());

                    System.out.println("++++ " + stringBuilder.toString());

                    Dataset dataForClassification = FileHandler.loadDataset(new File("" +
                            "./src/main/resources/tmpNewsTfIds.data"), ",");
                    int correct = 0, wrong = 0;

                    for (Instance inst : dataForClassification) {
                        Object predictedClassValue = svm.classify(inst);
                        System.out.println("Original : " + originalWeight + " Predicted : " + Integer.parseInt((String) predictedClassValue));
                        if( originalWeight == ((Integer.parseInt((String) predictedClassValue)))){
                            correctCount ++;
                        }
                        totalCount ++;
                    }
                    count ++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                if(count > 100){
//                    break;
//                }
            }

            System.out.println("News Count : " + count);

//            count = 0;
//            for (AnnouncementData announcementData : TextClassificationStore.getInstance().loadAnnouncementsFromFile()) {
//                try {
//                    //Classify a single sentence
//                    String sentence = announcementData.getAnnHeading();// + announcementData.getAnnBody();
//
////                    System.out.println(">>> " +sentence);
//                    String originalTrend = announcementData.getTrend() > 0 ? "positive" : "negative";
//
//                    StringBuilder stringBuilder = new StringBuilder();
//
//                    List<String> wordList = TextUtils.parseSentences(ExudeData.getInstance()
//                            .filterStoppingsKeepDuplicates
//                                    (sentence));
//                    for (String word : wordList) {
//                        String refactored = (String)(snowballStemmer.stem(word.toLowerCase().replaceAll("[0-9]", "")));
//                        if(refactored.length() > 1 && globalWordsList.contains(refactored)) {
////                            System.out.println("+++ word found : " + refactored );
//                            stringBuilder.append(refactored.hashCode());
//                            stringBuilder.append(",");
//                        }
//                    }
//
//                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
////                stringBuilder.append("\n");
////                System.out.println("SVM Predict Data: " + stringBuilder.toString());
//
//                    this.writeToFile("./src/main/resources/tmpanns.data", stringBuilder.toString());
//
//                    Dataset dataForClassification = FileHandler.loadDataset(new File("./src/main/resources/tmpanns.data"), ",");
//        /* Counters for correct and wrong predictions. */
//                    int correct = 0, wrong = 0;
//        /* Classify all instances and check with the correct class values */
//                    for (Instance inst : dataForClassification) {
//                        Object predictedClassValue = svm.classify(inst);
////                        System.out.println("Predicted: " + predictedClassValue + " : " + originalTrend);
//                        System.out.println("Original : " + originalTrend + " Predicted : " + ((Double.parseDouble((String) predictedClassValue)) > 0 ? "positive" : "negative"));
//                        if (originalTrend.equalsIgnoreCase(((Double.parseDouble((String) predictedClassValue)) > 0 ?
//                                "positive" : "negative"))) {
//                            correctCount++;
//                        }
//                        totalCount++;
//                    }
//                    count ++;
////                System.out.println("Success pct : " + (correctCount * 100)/totalCount);
//                } catch (InvalidDataException e) {
////                    e.printStackTrace();
//                }
////                if(count > 500){
////                    break;
////                }
//            }
//            System.out.println("Announcement Count : " + count);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("========> SVM Total news used : " + totalCount + " : " + (correctCount * 100)/totalCount);
    }

    private void trainClassifier(SqlConnector sqlConnector){
        StringBuilder stringBuilder = new StringBuilder();
        List<String> wordMaster = TextClassificationStore.getInstance().loadWordMasterFile();
        int count = 0;
        for (WeightedDocument document : TextClassificationStore.getWeightedDocumentList()) {
            stringBuilder.append(document.getWeight());
            for (String word : wordMaster) {
                if(document.getDocument().contains(word)){
                    stringBuilder.append(",");
                    stringBuilder.append("1");
                } else {
                    stringBuilder.append(",");
                    stringBuilder.append("0");
                }
            }
            stringBuilder.append("\n");
        }

        System.out.println("SVM Train Data: Completed " + TextClassificationStore.getWeightedDocumentList().size());

        this.writeToFile("./src/main/resources/svmTfIdf.data", stringBuilder.toString());


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

            String createTableSQL = "SELECT AVG(count) FROM msc.classifier_tf_idf";
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

    public static void dumpSvmClassifier(net.sf.javaml.classification.Classifier svm){
        try{
            FileOutputStream fos= new FileOutputStream("svmClassifier.dat");
            ObjectOutputStream oos= new ObjectOutputStream(fos);
            oos.writeObject(svm);
            oos.close();
            fos.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public net.sf.javaml.classification.Classifier loadSvmClassifier(){
        net.sf.javaml.classification.Classifier svm = null;
        try
        {
            FileInputStream fis = new FileInputStream("svmClassifier.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            svm = (net.sf.javaml.classification.Classifier) ois.readObject();
            ois.close();
            fis.close();
        }catch(Exception ioe){
            ioe.printStackTrace();
        }

        return svm;
    }
}

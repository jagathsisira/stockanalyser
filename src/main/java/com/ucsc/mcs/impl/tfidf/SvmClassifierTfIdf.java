package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.data.NewsData;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.ucsc.mcs.impl.tfidf.connector.WeightedDocument;
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import libsvm.LibSVM;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;
import net.sf.javaml.tools.weka.WekaClassifier;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.log4j.Logger;

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
    private static final Logger logger = Logger.getLogger(SvmClassifierTfIdf.class);

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
        int absCorrectCount = 0;
        int signCorrectCount = 0;
        int totalCount = 0;

        try {
        /* Load a data set */
            Dataset data = FileHandler.loadDataset(new File("./src/main/resources/svmTfIdf.data"), 0, ",");
        /*
         * Contruct a LibSVM classifier with default settings.
         *
         */
            long startTime = System.currentTimeMillis();
            logger.info("Start building classifier " + startTime);

            net.sf.javaml.classification.Classifier svm = new KNearestNeighbors(5);
            svm.buildClassifier(data);
//            dumpSvmClassifier(svm);
//            net.sf.javaml.classification.Classifier svm =loadSvmClassifier();
            long endTime = System.currentTimeMillis();
            logger.info("Finished building classifier " + endTime + " in " + ((endTime - startTime)/1000) + " " +
                    "seconds");

        /*
         * Load a data set, this can be a different one, but we will use the
         * same one.
         */

            int count = 0;
            int predictCount = 0;
            int duplicatePredictCount = 0;
            int offSet = 0;

            List<String> predictedList = new ArrayList<>();
            for (NewsData newsData : TextClassificationStore.getInstance().loadNewsFromFile()) {
                offSet ++;
//                if(offSet > 2000){
//                    break;
//                }
                if(offSet <= 10000){
                    continue;
                }
                int wordMasterMatchingCount = 0;
                try {
                    //Classify a single sentence
                    String sentence = newsData.getNewsHeading() + " " + newsData.getNewsBody();
                    int originalWeight = newsData.getWeight() * newsData.getTrend();

                    StringBuilder stringBuilder = new StringBuilder();

                    List<String> document = TextUtils.parseSentences(ExudeData
                            .getInstance().filterStoppingsKeepDuplicates(sentence), true);
                    List<String> wordMaster = TextClassificationStore.getInstance().loadWordMasterFile();
                    for(String masterWord: wordMaster) {
                        if(document.contains(masterWord)) {
                            stringBuilder.append("1");
                            stringBuilder.append(",");
                            wordMasterMatchingCount ++;
                        } else {
                            stringBuilder.append("0");
                            stringBuilder.append(",");
                        }
                    }
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    if(predictedList.contains(stringBuilder.toString())){
                        duplicatePredictCount ++;
                        logger.info("Duplicate Prediction : " + duplicatePredictCount + " : " + newsData.getNewsId()
                                + document.toString());
                        continue;
                    } else {
                        predictedList.add(stringBuilder.toString());
                    }
                    predictCount ++;

                    this.writeToFile("./src/main/resources/tmpNewsTfIds.data", stringBuilder.toString());

                    logger.info("Predicting : " + wordMasterMatchingCount + " : " + newsData.getNewsId() + " " +
                            document
                            .toString());
                    logger.info("Predicting : " + wordMasterMatchingCount + " : " + newsData.getNewsId() + " " + stringBuilder.toString());

                    if(predictCount > 2000){
                        break;
                    }

//                    System.out.println("++++ " + stringBuilder.toString());

                    Dataset dataForClassification = FileHandler.loadDataset(new File("" +
                            "./src/main/resources/tmpNewsTfIds.data"), ",");
                    int correct = 0, wrong = 0;

                    for (Instance inst : dataForClassification) {
                        Object predictedClassValue = svm.classify(inst);
                        if( originalWeight == ((Integer.parseInt((String) predictedClassValue)))){
                            correctCount ++;
                        }
                        if( Math.abs(originalWeight) == Math.abs(((Integer.parseInt((String) predictedClassValue))))){
                            absCorrectCount ++;
                        }

                        if( Math.signum(originalWeight) == Math.signum(((Integer.parseInt((String) predictedClassValue))))){
                            signCorrectCount ++;
                        }
                        totalCount ++;
                        logger.info("Original : " + originalWeight + " Predicted : " + Integer.parseInt((String)
                                predictedClassValue) + " : News ID : " + newsData.getNewsId() + " Total : " +
                                totalCount + " Success: " + (correctCount * 100)/totalCount + " Abs Success: " +
                                (absCorrectCount * 100)/totalCount + " Sign Success: " +
                                (signCorrectCount * 100)/totalCount);
                    }
                    count ++;
                } catch (Exception e) {
                    logger.info("Error in predicting news ", e);
                }
//                if(count > 100){
//                    break;
//                }
            }

            logger.info("News Count : " + count);

//            count = 0;
//            for (AnnouncementData announcementData : TextClassificationStore.getInstance().loadAnnouncementsFromFile()) {
//                try {
//                    //Classify a single sentence
//                    String sentence = announcementData.getAnnHeading();
//                    int originalWeight = announcementData.getWeight() * announcementData.getTrend();
//
//                    StringBuilder stringBuilder = new StringBuilder();
//
//                    List<String> document = TextUtils.parseSentences(ExudeData
//                            .getInstance().filterStoppingsKeepDuplicates(sentence), true);
//                    List<String> wordMaster = TextClassificationStore.getInstance().loadWordMasterFile();
//                    for (String masterWord : wordMaster) {
//                        if (document.contains(masterWord)) {
//                            stringBuilder.append("1");
//                            stringBuilder.append(",");
//                        } else {
//                            stringBuilder.append("0");
//                            stringBuilder.append(",");
//                        }
//                    }
//                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
//
//                    this.writeToFile("./src/main/resources/tmpAnnTfIds.data", stringBuilder.toString());
//
////                    System.out.println("++++ " + stringBuilder.toString());
//
//                    Dataset dataForClassification = FileHandler.loadDataset(new File("" +
//                            "./src/main/resources/tmpAnnTfIds.data"), ",");
//                    int correct = 0, wrong = 0;
//
//                    for (Instance inst : dataForClassification) {
//                        Object predictedClassValue = svm.classify(inst);
//                        logger.info("Original : " + originalWeight + " Predicted : " + Integer.parseInt((String)
//                                predictedClassValue) + " : Ann ID : " + announcementData.getAnnId());
//                        if (originalWeight == ((Integer.parseInt((String) predictedClassValue)))) {
//                            correctCount++;
//                        }
//                        totalCount++;
//                    }
//                    count++;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                logger.info("Announcement Count : " + count);
//            }
        } catch (IOException e) {
            logger.info("Error in predicting data ", e);
        }
        logger.info("========> SVM Total entries used : " + totalCount + " : " + (correctCount * 100)
                /totalCount);
    }

    private void trainClassifier(SqlConnector sqlConnector){
        StringBuilder stringBuilder = new StringBuilder();
        List<String> trainedList = new ArrayList<>();
        List<String> wordMaster = TextClassificationStore.getInstance().loadWordMasterFile();
        TextClassificationStore.loadWeightedDocsFromFile();
        int count = 0;
        int duplicateCount = 0;
        for (WeightedDocument document : TextClassificationStore.getWeightedDocumentList()) {
            logger.info(">> " + document.getDocument());
            StringBuilder stringBuilder2 = new StringBuilder();
            StringBuilder stringBuilder3 = new StringBuilder();
//            stringBuilder.append(document.getWeight());
            stringBuilder2.append(document.getWeight());
            stringBuilder3.append("0000");
            for (String word : wordMaster) {
                if(document.getDocument().contains(word)){
//                    stringBuilder.append(",");
//                    stringBuilder.append("1");
                    stringBuilder2.append(",");
                    stringBuilder2.append("1");
                    stringBuilder3.append(",");
                    stringBuilder3.append("1");
                } else {
//                    stringBuilder.append(",");
//                    stringBuilder.append("0");
                    stringBuilder2.append(",");
                    stringBuilder2.append("0");
                    stringBuilder3.append(",");
                    stringBuilder3.append("0");
                }
            }
            if(trainedList.contains(stringBuilder3.toString())){
                duplicateCount ++;
                logger.info("Duplicated Training : " + duplicateCount + " : " + document.getId() + " " + document
                        .getDocument().toString());
                logger.info("Duplicated Training : " + duplicateCount + " : " +  document.getId() + " " +
                        stringBuilder2.toString());
                continue;
            } else {
                trainedList.add(stringBuilder3.toString());
            }
            stringBuilder.append(stringBuilder2.toString());
            stringBuilder.append("\n");
            logger.info("Training : " + count + " : " + document.getId() + " " + document.getDocument().toString());
            logger.info("Training : " + count + " : " + document.getId() + " " + stringBuilder2.toString());
            count ++;
            if(count > 10000){
                break;
            }
        }

        logger.info("SVM Train Data: Completed " + TextClassificationStore.getWeightedDocumentList().size() + " " +
                "Actual : " + count);

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
        logger.info("avgCount " + avgCount + " : " + new Double(avgCount).intValue());
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

package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.data.NewsData;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.ucsc.mcs.impl.tfidf.connector.WeightedDocument;
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by JagathA on 12/24/2017.
 */
public class NaiveBayesClassifierTfIdf {

    private static final Logger logger = Logger.getLogger(NaiveBayesClassifierTfIdf.class);

    private static final NaiveBayesClassifierTfIdf naiveBayesClassifierTfIdf = new NaiveBayesClassifierTfIdf();

    private Map<Integer,List<String>> signedWords = new HashMap<>();

    private NaiveBayesClassifierTfIdf() {
    }

    public static NaiveBayesClassifierTfIdf getInstance() {
        return naiveBayesClassifierTfIdf;
    }

    public void predict(SqlConnector connector, int predictionStart, int predictionFinish) {
        // Two examples to learn from.
        ArrayList<Boolean> results = new ArrayList<>();

        signedWords.clear();

        trainClassifier(connector);

        // Create a new bayes classifier with string categories and string features.
        Classifier<String, String> bayes = new BayesClassifier<String, String>();

        // Learn by classifying examples.
        // New categories can be added on the fly, when they are first used.
        // A classification consists of a category and a list of features
        // that resulted in the classification in that category.

        logger.info("Start Learning...............");
        for(Integer weight: signedWords.keySet()){
            bayes.learn("" + weight, signedWords.get(weight));
            logger.info("Completed : " + weight + " size : " + signedWords.get(weight).size());
        }
        logger.info("Finished Learning...............");

        int predictionCount = 0;
        int totalCount = 0;
        int correctCount = 0;

        for (NewsData newsData : TextClassificationStore.getInstance().loadNewsFromFile()) {
            totalCount++;

            if(totalCount <= predictionStart || totalCount > predictionFinish){
                continue;
            }

            if(totalCount > predictionFinish){
                break;
            }

            predictionCount ++;
            try {
                //Classify a single sentence
                String sentence = newsData.getNewsHeading();
                String originalWeight = ""+ newsData.getWeight();

                StringBuilder stringBuilder = new StringBuilder();

                List<String> document = TextUtils.parseSentences(ExudeData
                        .getInstance().filterStoppingsKeepDuplicates(sentence), true);
//                List<String> wordMaster = TextClassificationStore.getInstance().loadWordMasterFile();
//                for (String masterWord : wordMaster) {
//                    if (document.contains(masterWord)) {
//                        stringBuilder.append("1");
//                        stringBuilder.append(",");
//                        wordMasterMatchingCount++;
//                    } else {
//                        stringBuilder.append("-1");
//                        stringBuilder.append(",");
//                    }
//                }
//                stringBuilder.deleteCharAt(stringBuilder.length() - 1);


                String predictedTrend = bayes.classify(document).getCategory();

//                System.out.println(" Original : " + originalWeight + " : Predicted : " + predictedTrend);
                if (originalWeight.equalsIgnoreCase(predictedTrend)){
                    correctCount ++;
                }

//                System.out.println( "Original : " + originalTrend + " Predicted : " + predictedTrend + " : " +
//                        ((correctCount * 100)/totalCount));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

//        count = 0;
//
//        for(AnnouncementData announcementData : TextClassificationStore.getInstance().loadAnnouncementsFromFile()) {
//            //Classify a single sentence
//            try {
//                String sentence = announcementData.getAnnHeading();
//                String originalTrend = announcementData.getTrend() > 0 ? "positive" : "negative";
//                List<String> wordList = TextUtils.parseSentences(ExudeData.getInstance()
//                        .filterStoppingsKeepDuplicates
//                                (sentence));
//                List<String> updatedList = new ArrayList<>();
//                for (int i=0;i < wordList.size(); i++) {
//                    String word = wordList.get(i).toLowerCase().replaceAll("[0-9]", "");
//                    String refactored = (String)(snowballStemmer.stem(word));
//                    if(refactored.length() > 1) {
//                        updatedList.add(refactored);
//                        System.out.println(">>>><<<< " + refactored);
//                    }
//                }
//                String predictedTrend = bayes.classify(updatedList).getCategory();
//                totalCount ++;
//                count ++;
//                if (originalTrend.equalsIgnoreCase(predictedTrend)){
//                    correctCount ++;
//                }
//
////                System.out.println( "Original : " + originalTrend + " Predicted : " + predictedTrend + " : " +
////                        ((correctCount * 100)/totalCount));
//                if(count > 1000){
//                    break;
//                }
//            } catch (InvalidDataException e) {
//                e.printStackTrace();
//            }
//        }

        logger.info("========> Naive Bayes Total Prediction window : " + " Start:" + predictionStart +
                " " +
                "Finish: " +
                predictionFinish + " " + predictionCount + " : " + (correctCount * 100)/predictionCount);

        bayes.setMemoryCapacity(500);
    }

    private void trainClassifier(SqlConnector sqlConnector){
        List<WeightedDocument> documents = TextClassificationStore.loadWeightedDocsFromFile();
        int count = 0;
        for(WeightedDocument document : documents){
            List<String> list = signedWords.get(document.getWeight());
            if(list == null){
                list  = new ArrayList<>();
                signedWords.put(document.getWeight(), list);
            }

//            logger.info("Merging : old : " + document.getWeight() + " : " + list.size() + " : " + document
//                    .getDocument().size());
            mergeWordList(document.getDocument(), list);
//            logger.info("Merged : new  : "+ document.getWeight() + " : " + list.size());
            count ++;
        }

        logger.info("Training completed with size : " + signedWords);
    }

    public static void mergeWordList(List<String> newList, List<String> oldList) {
        //populate list
        for (String word : newList) {
            if (!oldList.contains(word)) {
                oldList.add(word);
            }
        }

        Collections.sort(oldList, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
    }
}

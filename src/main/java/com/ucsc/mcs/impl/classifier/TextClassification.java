package com.ucsc.mcs.impl.classifier;

import com.datumbox.framework.applications.nlp.TextClassifier;
import com.datumbox.framework.common.Configuration;
import com.datumbox.framework.common.utilities.RandomGenerator;
import com.datumbox.framework.core.common.dataobjects.Record;
import com.datumbox.framework.core.common.text.extractors.NgramsExtractor;
import com.datumbox.framework.core.machinelearning.MLBuilder;
import com.datumbox.framework.core.machinelearning.classification.MultinomialNaiveBayes;
import com.datumbox.framework.core.machinelearning.featureselection.ChisquareSelect;
import com.datumbox.framework.core.machinelearning.modelselection.metrics.ClassificationMetrics;
import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.data.NewsData;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JagathA on 12/3/2017.
 */
public class TextClassification {
    private static TextClassification textClassification = new TextClassification();

    private TextClassification() {
    }

    public static TextClassification getInstance() {
        return textClassification;
    }

    public static void main(String[] args) {
//        TextClassification textClassification = getInstance();
//        try {
////            classifyText();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
    }

//    private static void classifyText() throws URISyntaxException {
    public void classifyText() {
        /**
         * There are 5 configuration files in the resources folder:
         *
         * - datumbox.configuration.properties: It defines for the default storage engine (required)
         * - datumbox.concurrencyconfiguration.properties: It controls the concurrency levels (required)
         * - datumbox.inmemoryconfiguration.properties: It contains the configurations for the InMemory storage engine (required)
         * - datumbox.mapdbconfiguration.properties: It contains the configurations for the MapDB storage engine (optional)
         * - logback.xml: It contains the configuration file for the logger (optional)
         */

        //Initialization
        //--------------
        RandomGenerator.setGlobalSeed(42L); //optionally set a specific seed for all Random objects
        Configuration configuration = Configuration.getConfiguration(); //default configuration based on properties file
        //configuration.setStorageConfiguration(new InMemoryConfiguration()); //use In-Memory engine (default)
        //configuration.setStorageConfiguration(new MapDBConfiguration()); //use MapDB engine
        //configuration.getConcurrencyConfiguration().setParallelized(true); //turn on/off the parallelization
        //configuration.getConcurrencyConfiguration().setMaxNumberOfThreadsPerTask(4); //set the concurrency level



        //Reading Data
        //------------
        Map<Object, URI> datasets = new HashMap<>(); //The examples of each category are stored on the same file, one example per row.
        try {
//            datasets.put("positive", TextClassification.class.getClassLoader().getResource("./src/main/resources/positive.txt").toURI());
            datasets.put("positive", new File("./src/main/resources/positive.txt").toURI());
            datasets.put("negative", new File("./src/main/resources/negative.txt").toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Setup Training Parameters
        //-------------------------
        TextClassifier.TrainingParameters trainingParameters = new TextClassifier.TrainingParameters();

        //numerical scaling configuration
        trainingParameters.setNumericalScalerTrainingParameters(null);

        //Set feature selection configuration
        trainingParameters.setFeatureSelectorTrainingParametersList(Arrays.asList(new ChisquareSelect.TrainingParameters()));

        //Set text extraction configuration
        trainingParameters.setTextExtractorParameters(new NgramsExtractor.Parameters());

        //Classifier configuration
        trainingParameters.setModelerTrainingParameters(new MultinomialNaiveBayes.TrainingParameters());



        //Fit the classifier
        //------------------
        TextClassifier textClassifier = MLBuilder.create(trainingParameters, configuration);
        textClassifier.fit(datasets);
//        textClassifier.save("SentimentAnalysis");



        //Use the classifier
        //------------------

        //Get validation metrics on the dataset
        ClassificationMetrics vm = textClassifier.validate(datasets);

        ArrayList<String> results = new ArrayList<>();

        for(NewsData newsData : TextClassificationStore.getInstance().getNewsList()) {
            //Classify a single sentence
            String sentence = newsData.getNewsHeading();
            Record r = textClassifier.predict(sentence);

            System.out.println("Classifing sentence: \"" + sentence + "\"");
            System.out.println("Predicted class: " + r.getYPredicted() + " Original : " + (newsData.getTrend() > 0 ? "positive" : "negative"));
            System.out.println("Probability: " + r.getYPredictedProbabilities().get(r.getYPredicted()));

            System.out.println("Classifier Accuracy: " + vm.getAccuracy());
            results.add("Predicted class: " + r.getYPredicted() + "\tOriginal : " +
                    (newsData.getTrend() > 0 ? "positive" : "negative") + "\t = " +
                    getSuccess((newsData.getTrend() > 0 ? "positive" : "negative"),(String)(r.getYPredicted())));
        }

        for(AnnouncementData announcementData : TextClassificationStore.getInstance().getAnnouncementsList()) {
            //Classify a single sentence
            String sentence = announcementData.getAnnHeading();
            Record r = textClassifier.predict(sentence);

            System.out.println("Classifing sentence: \"" + sentence + "\"");
            System.out.println("Predicted class: " + r.getYPredicted() + " Original : " + (announcementData.getTrend() > 0 ? "positive" : "negative"));
            System.out.println("Probability: " + r.getYPredictedProbabilities().get(r.getYPredicted()));

            System.out.println("Classifier Accuracy: " + vm.getAccuracy());
            results.add("Predicted class: " + r.getYPredicted() + "\tOriginal : " +
                    (announcementData.getTrend() > 0 ? "positive" : "negative") + "\t = " +
                    getSuccess((announcementData.getTrend() > 0 ? "positive" : "negative"),(String)(r.getYPredicted())));
        }

        System.out.println("========================================");

        for(String result : results){
            System.out.println(result);
        }

        System.out.println("========================================");

        //Clean up
        //--------

        //Delete the classifier. This removes all files.
        textClassifier.delete();
    }

    private boolean getSuccess(String original, String result){
        return original.equalsIgnoreCase(result);
    }

}

package com.ucsc.mcs.test;

import de.daslaboratorium.machinelearning.classifier.Classifier;
import de.daslaboratorium.machinelearning.classifier.bayes.BayesClassifier;

import java.util.Arrays;

/**
 * Created by JagathA on 10/1/2017.
 */
public class SentimentAnalysisTest {

    public SentimentAnalysisTest() {
        analyse();
    }

    public static void main(String[] args) {
        new SentimentAnalysisTest();
    }

    private void analyse(){
        // Create a new bayes classifier with string categories and string features.
        Classifier<String, String> bayes = new BayesClassifier<String, String>();

// Two examples to learn from.
        String[] positiveText = "I love sunny days".split("\\s");
        String[] negativeText = "I hate will a days".split("\\s");
        String[] neutralText = "I hate a days".split("\\s");

// Learn by classifying examples.
// New categories can be added on the fly, when they are first used.
// A classification consists of a category and a list of features
// that resulted in the classification in that category.
        bayes.learn("positive", Arrays.asList(positiveText));
        bayes.learn("negative", Arrays.asList(negativeText));
        bayes.learn("neutral", Arrays.asList(neutralText));

// Here are two unknown sentences to classify.
        String[] unknownText1 = "today is a days".split(" ");
        String[] unknownText2 = "there will be hate rain days".split(" ");

        System.out.println( // will output "positive"
                bayes.classify(Arrays.asList(unknownText1)).getCategory());
        System.out.println( // will output "negative"
                bayes.classify(Arrays.asList(unknownText2)).getCategory());

// Get more detailed classification result.
        ((BayesClassifier<String, String>) bayes).classifyDetailed(
                Arrays.asList(unknownText1));

// Change the memory capacity. New learned classifications (using
// the learn method) are stored in a queue with the size given
// here and used to classify unknown sentences.
        bayes.setMemoryCapacity(500);
    }
}

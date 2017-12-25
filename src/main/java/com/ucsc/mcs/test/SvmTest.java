package com.ucsc.mcs.test;

import com.wanasit.litesvm.BinaryClassifier;
import com.wanasit.litesvm.BinarySample;
import com.wanasit.litesvm.LiteSVM;
import libsvm.LibSVM;
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.filter.discretize.EqualWidthBinning;
import net.sf.javaml.tools.data.FileHandler;
import org.testng.collections.Lists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by JagathA on 12/24/2017.
 */
public class SvmTest {
    public static void main(String[] args) throws Exception {

        String word1 = "release";
        String word2 = "sun";
        String word3 = "process";
        String word4 = "success";
        String word5 = "increase";
        String word6 = "profit";
        String word7 = "value";
        String word8 = "market";
        svm();
//        bayes();


    }



    private static void svm() throws Exception {
         /* Load a data set */
            Dataset data = FileHandler.loadDataset(new File("./src/main/resources/iris-var.data"),0, ",");
        /*
         * Contruct a LibSVM classifier with default settings.
         */
            Classifier svm = new LibSVM();
            svm.buildClassifier(data);

        /*
         * Load a data set, this can be a different one, but we will use the
         * same one.
         */
            Dataset dataForClassification = FileHandler.loadDataset(new File("./src/main/resources/iris-predict.data"),",");
        /* Counters for correct and wrong predictions. */
            int correct = 0, wrong = 0;
        /* Classify all instances and check with the correct class values */
            for (Instance inst : dataForClassification) {
                Object predictedClassValue = svm.classify(inst);
                Object realClassValue = inst.classValue();
                System.out.println("Predicted: " + predictedClassValue + " Actual: " + realClassValue);
                if (predictedClassValue.equals(realClassValue))
                    correct++;
                else
                    wrong++;
            }
            System.out.println("Correct predictions  " + correct);
            System.out.println("Wrong predictions " + wrong);

    }
    private static void bayes() throws Exception {

		/* Load a data set */
//        Dataset data = FileHandler.loadDataset(new File("./src/main/resources/iris.data"), 4, ",");
        Dataset data = TutorialData.IRIS.load();


		/* Discretize through EqualWidtBinning */
        EqualWidthBinning eb = new EqualWidthBinning(20);
        System.out.println("Start discretisation");
        eb.build(data);
        Dataset ddata = data.copy();
        eb.filter(ddata);

        boolean useLaplace = true;
        boolean useLogs = true;
        Classifier nbc = new NaiveBayesClassifier(useLaplace, useLogs, false);
        nbc.buildClassifier(data);

        Dataset dataForClassification = TutorialData.IRIS.load();

		/* Counters for correct and wrong predictions. */
        int correct = 0, wrong = 0;

		/* Classify all instances and check with the correct class values */
        for (Instance inst : dataForClassification) {
            eb.filter(inst);
            Object predictedClassValue = nbc.classify(inst);
            Object realClassValue = inst.classValue();
            if (predictedClassValue.equals(realClassValue))
                correct++;
            else {
                wrong++;

            }

        }
        System.out.println("===== Bayes =======");
        System.out.println("correct " + correct);
        System.out.println("incorrect " + wrong);

    }

}

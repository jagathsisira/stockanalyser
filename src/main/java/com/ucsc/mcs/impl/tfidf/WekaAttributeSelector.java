package com.ucsc.mcs.impl.tfidf;

import net.sf.javaml.core.Dataset;
import net.sf.javaml.featureselection.ranking.RecursiveFeatureEliminationSVM;
import net.sf.javaml.featureselection.scoring.GainRatio;
import net.sf.javaml.tools.data.FileHandler;
import net.sf.javaml.tools.weka.WekaAttributeSelection;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.Ranker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JagathA on 1/28/2018.
 */
public class WekaAttributeSelector {

    public WekaAttributeSelector() {
    }

    public static void selectAttributes(){

        List<String> wordMaster = TextClassificationStore.getInstance().loadWordMasterFile();
        List<String> newWordMaster = new ArrayList<>();
        System.out.println("WM length : " + wordMaster.size());
        /* Load the iris data set */
        try {
            Dataset data = FileHandler.loadDataset(new File("./src/main/resources/svmTfIdf.data"),0,",");
            /* Create a feature scoring algorithm */
            GainRatio ga = new GainRatio();
/* Apply the algorithm to the data set */
            ga.build(data);
/* Print out the score of each attribute */
            int addedCount = 0;
            for (int i = 0; i < ga.noAttributes(); i++) {
                System.out.println(i + " " + ga.score(i));
                if(ga.score(i) != 0.0){
                    System.out.println("Score not 0 - adding " + i);
                    newWordMaster.add(wordMaster.get(i));
                    addedCount ++;
                }
            }
//
            TextClassificationStore.setWordMaster(newWordMaster);
            TextClassificationStore.dumpWordMaster();

            //------------------------------------------\

//            /* Create a feature ranking algorithm */
//            RecursiveFeatureEliminationSVM svmrfe = new RecursiveFeatureEliminationSVM(0.2);
///* Apply the algorithm to the data set */
//            svmrfe.build(data);
///* Print out the rank of each attribute */
//            for (int i = 0; i < svmrfe.noAttributes(); i++)
//                System.out.println(svmrfe.rank(i));

            //-------------------------------------
///*Create a Weka AS Evaluation algorithm */
//            ASEvaluation eval = new GainRatioAttributeEval();
///* Create a Weka's AS Search algorithm */
//            ASSearch search = new Ranker();
///* Wrap WEKAs' Algorithms in bridge */
//            WekaAttributeSelection wekaattrsel = new WekaAttributeSelection(eval,search);
///* Apply the algorithm to the data set */
//            wekaattrsel.build(data);
///* Print out the score and rank  of each attribute */
//            for (int i = 0; i< wekaattrsel.noAttributes(); i++) {
//                System.out.println("Attribute  " + i + "  Ranks  " + wekaattrsel.rank(i) + " and Scores " + wekaattrsel.score(i));
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

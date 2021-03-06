package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.tfidf.connector.MySqlConnector;

/**
 * Created by JagathA on 8/11/2017.
 */
public class StockAnalyserMain {

    public StockAnalyserMain() {
        execute();
    }

    public static void main(String[] args) {
        new StockAnalyserMain();
    }

    private void execute(){
//        DataCleanser dataCleanser = new DataCleanser(MySqlConnector.getInstance());
//        dataCleanser.cleanAndInsertInitialAnnData();
//        dataCleanser.cleanAndInsertInitialNewsData();
//        dataCleanser.cleanAndInsertInitialHistoryData();
//        HotSpotInspector hotSpotInspector = new HotSpotInspector(MySqlConnector.getInstance());
//        hotSpotInspector.getHotSpots();

        //General

//        NewsClassifier newsClassifier = new NewsClassifier(MySqlConnector.getInstance());
//        newsClassifier.classifyNews();
//
//        AnnouncementClassifier announcementClassifier = new AnnouncementClassifier(MySqlConnector.getInstance());
//        announcementClassifier.classifyAnnouncements();

//        TextClassificationStore.getInstance().updateClassifierDatabase(MySqlConnector.getInstance());
//        NaiveBayesClassifier.getInstance().predict(MySqlConnector.getInstance());
//        SvmClassifier.getInstance().predict(MySqlConnector.getInstance());

        //TFIDF

//        TfIdfAnnouncementClassifier tfIdfAnnouncementClassifier = new TfIdfAnnouncementClassifier(MySqlConnector.getInstance());
//        tfIdfAnnouncementClassifier.classifyAnnouncements();

        TfIdfNewsClassifier tfIdfNewsClassifier = new TfIdfNewsClassifier(MySqlConnector.getInstance());
        tfIdfNewsClassifier.classifyNews();

        TextClassificationStore.dumpWeightedDocs();
        TextClassificationStore.generateWordMaster();


//        TextClassificationStore.getInstance().updateClassifierDatabaseTfIdf(MySqlConnector.getInstance());
//        TextClassificationStore.getInstance().updateClassifierTextFileTfIdf();
        SvmClassifierTfIdf.getInstance().predict(MySqlConnector.getInstance());
//        WekaDataClassifier.getInstance().predict(MySqlConnector.getInstance());
//        CrossValidationClassifier.getInstance().predict(MySqlConnector.getInstance());
//        WekaAttributeSelector.selectAttributes();
//        KNearestNeighbourClassifier.getInstance().predict(MySqlConnector.getInstance());


    }

}

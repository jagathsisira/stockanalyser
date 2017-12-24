package com.ucsc.mcs.impl;

import com.ucsc.mcs.impl.classifier.TextClassification;
import com.ucsc.mcs.impl.connector.MySqlConnector;
import com.ucsc.mcs.impl.datamanage.AnnouncementClassifier;
import com.ucsc.mcs.impl.datamanage.NewsClassifier;
import com.ucsc.mcs.impl.classifier.TextClassificationStore;

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

        NewsClassifier newsClassifier = new NewsClassifier(MySqlConnector.getInstance());
        newsClassifier.classifyNews();

        AnnouncementClassifier announcementClassifier = new AnnouncementClassifier(MySqlConnector.getInstance());
        announcementClassifier.classifyAnnouncements();

//        TextClassificationStore.getInstance().updateClassifierDatabase(MySqlConnector.getInstance());
        TextClassificationStore.getInstance().printStoreStats();
//        TextClassificationStore.getInstance().updateClassifierTextFile();
        TextClassification.getInstance().classifyText();
    }
}

package com.ucsc.mcs.impl.tfidf;

import com.ucsc.mcs.impl.data.AnnouncementData;
import com.ucsc.mcs.impl.tfidf.connector.SqlConnector;
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JagathA on 1/20/2018.
 */
public class TfIdfAnnouncementClassifier {

    private SqlConnector sqlConnector = null;

    public TfIdfAnnouncementClassifier(SqlConnector sqlConnector){
        this.sqlConnector = sqlConnector;
    }

    public static void main(String[] args) {
        TfIdfAnnouncementClassifier tfIdfAnnouncementClassifier = new TfIdfAnnouncementClassifier(null);
        tfIdfAnnouncementClassifier.calculateTermWeights();
    }

    private void calculateTermWeights(){
        ArrayList<AnnouncementData> announcementDataArrayList = TextClassificationStore.getInstance().loadAnnouncementsFromFile();
        List<List<String>> documentList = new ArrayList<>();

        for(AnnouncementData announcementData : announcementDataArrayList){
            try {
                List<String> document = TextUtils.parseSentences(ExudeData
                        .getInstance().filterStoppingsKeepDuplicates(announcementData.getAnnHeading()), true);
                documentList.add(document);
            } catch (InvalidDataException e) {
                System.out.println("Error data Ann : " + announcementData.getAnnHeading());
            }
        }

        System.out.println("+++++++++++ Ann Docs Size " + documentList.size());
    }
}

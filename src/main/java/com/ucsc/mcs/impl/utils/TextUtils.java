package com.ucsc.mcs.impl.utils;

import com.ucsc.mcs.impl.data.TextEntry;
import com.ucsc.mcs.impl.classifier.TextClassificationStore;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by JagathA on 11/24/2017.
 */
public class TextUtils {

    private static String STOP_LIST_FILE = "stoplist.txt";

    private static Set<String> stopList = loadStopWords();

    public static void updateTextClassifier(String text){
        String[] textArray = text.split(" ");

        for(String word : textArray){
            ArrayList<TextEntry> textClassification = TextClassificationStore.getInstance().getTextClassificationsForText(word);

        }

    }

    private static Set<String> loadStopWords() {
        Set<String> stoplist = new HashSet<String>();

        try {
            InputStream in = new FileInputStream(STOP_LIST_FILE);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                stoplist.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stoplist;
    }


    public static List<String> parseSentences(String sentence) {
        String[] words = sentence.replaceAll("\\p{Punct}", "")
                .toLowerCase().split("\\s");
        List<String> wordList = new ArrayList<String>();
        for (String word : words) {
            word = word.trim();
            if (word.length() > 0 && !stopList.contains(word)) {
                wordList.add(word);
            }
        }

        return wordList;
    }

    public static void writeToFile(String fileName, ArrayList<String> data) throws FileNotFoundException, IOException {
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(fileName);

            for(String word : data){
                out.write(word.getBytes());
                out.write(" ".getBytes());
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}

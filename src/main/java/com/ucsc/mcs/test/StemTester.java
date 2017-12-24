package com.ucsc.mcs.test;

import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by JagathA on 12/2/2017.
 */
public class StemTester {

    public static void main(String[] args) {
        String sentence = "she went home with his son by bus from London Euston station 12212 abc";
        SnowballStemmer snowballStemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
        System.out.println(snowballStemmer.stem("checked"));

        try {
            System.out.println(ExudeData.getInstance().filterStoppingsKeepDuplicates(sentence));
        } catch (InvalidDataException e) {
            e.printStackTrace();
        }

        System.out.println(parseDocuments(sentence, loadStopWords("stoplist.txt")));


    }

    private static Set<String> loadStopWords(String filename) {
        Set<String> stoplist = new HashSet<String>();

        try {
            InputStream in = new FileInputStream(filename);
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


    private static List<String> parseDocuments(String sentence , Set<String> stopwords) {

            String[] words = sentence.replaceAll("\\p{Punct}", "")
                    .toLowerCase().split("\\s");
            List<String> wordList = new ArrayList<String>();
            for(String word: words) {
                word = word.trim();
                if (word.length() > 0 && !stopwords.contains(word)) {
                    wordList.add(word);
                }

        }

        return wordList;
    }
}

package com.ucsc.mcs.impl.tfidf;

/**
 * Created by JagathA on 11/24/2017.
 */
public class TextEntry {

    private String text;
    private double score;

    public TextEntry(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String toString(){
        return new String("TextEntry : " + text + ":" + score);
    }
}

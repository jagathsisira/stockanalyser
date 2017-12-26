package com.ucsc.mcs.test;

import org.jsoup.Jsoup;

/** Created by JagathA on 12/26/2017. */
public class TextCleaner {

  public TextCleaner() {
    cleanText();
  }

  public static void main(String[] args) {
    new TextCleaner();
  }

  private void cleanText() {
      String news = "\"<p><span class=\"\"data1_En\"\">Finance \n" +
              " House (FH) has been awarded the Best Business Finance Company in the Middle East and the Best Retail Finance Company in the Middle East for the second consecutive year at the Banker Middle East Industry Awards \u200E\u200E2012 ceremony.</span></p>\n" +
              "<p><span class=\"\"data1_En\"\">For his part, Mohammed Abdulla Alqubaisi, FH Chairman, said: \"\"These accolades show the indelible mark that we have built in the region's banking industry. We have achieved major breakthroughs and our excellence in financial services is empowered by aspiration to meet clients' needs.\"\"</span> </p>\n" +
              "<p><span class=\"\"data1_En\"\">The top official thanked the company’s professional staff for experience and commitment, and clients for their trust in the company.<br />\n" +
              "<br />\n" +
              "</span></p>\"\n";

      String cleanedText = Jsoup.parse(news).text();
    System.out.println("+++  " + news);
    System.out.println(">> " + cleanedText);
    System.out.println(">> " + cleanedText.replaceAll("'"," ").replaceAll(",", " "));
  }
}

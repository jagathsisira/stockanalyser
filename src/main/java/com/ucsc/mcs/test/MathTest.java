package com.ucsc.mcs.test;

/**
 * Created by JagathA on 1/11/2018.
 */
public class MathTest {

  public static void main(String[] args) {
      double positive = 5.4;
      double negative = -0.6;

    System.out.println(Math.ceil(positive) + " : " + Math.ceil(Math.abs(negative)));
    System.out.println(Math.round(positive) + " : " + Math.round(Math.abs(negative)));

    double value1 = 44.0;
    double value2 = 65.0;
    double value3 = -65.0;
    double value4 = -445.0;

//    System.out.println(">> " + (int)((value1 + 99) / 100 ) * 100);
//    System.out.println(">> " + (int)((value2 + 99) / 100 ) * 100);
//    System.out.println(">> " + (int)((value3 + 99) / 100 ) * 100);
//    System.out.println(">> " + (int)((value4 + 99) / 100 ) * 100);
    System.out.println(">> " + (int)(Math.round( value1 / 100.0) * 100) );
    System.out.println(">> " + (int)(Math.round( value2 / 100.0) * 100) );
    System.out.println(">> " + (int)(Math.round( value3 / 100.0) * 100) );
    System.out.println(">> " + (int)(Math.round( value4 / 100.0) * 100) );
  }
}

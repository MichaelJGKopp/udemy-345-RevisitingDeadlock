package dev.lpa;

import java.io.File;

public class ThreadProblems {

  public static void main(String[] args) {

    File resourceA = new File("inputData.csv");
    File resourceB = new File("outputData.json");

    Thread threadA = new Thread(() -> {
      String threadName = Thread.currentThread().getName();
      System.out.println(threadName + " attempting to lock resourceA (csv)");
      synchronized (resourceA) {
        System.out.println(threadName + " has lock on resourceA (csv)");
        try {
          Thread.sleep(1_000); // represents some kind of work
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      System.out.println(threadName + " has released lock on resourceA (csv)");
    }, "\u001B[34mTHREAD-A");
  }
}

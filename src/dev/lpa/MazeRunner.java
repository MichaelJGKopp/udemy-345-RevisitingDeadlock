package dev.lpa;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

record Participant(String name, String searchingFor, Maze maze, int[] startingPosition) {

  Participant {
    maze.moveLocation(startingPosition[0], startingPosition[1], name);
  }
}

class ParticipantThread extends Thread {

  public final Participant participant;

  public ParticipantThread(Participant participant) {
    super(participant.name());
    this.participant = participant;
  }

  @Override
  public void run() {

    int[] lastSpot = participant.startingPosition();
    Maze maze = participant.maze();

    while (true) {
      int[] newSpot = maze.getNextLocation(lastSpot);
      try {
        Thread.sleep(500);
        if (maze.searchCell(participant.searchingFor(), newSpot, lastSpot)) {
          System.out.printf("%s found %s at %s!%n", participant.name(), participant.searchingFor(),
            Arrays.toString(newSpot));
          break;
        }
//        synchronized (maze) {
          maze.moveLocation(newSpot[0], newSpot[1], participant.name());
//        }
        lastSpot = new int[]{newSpot[0], newSpot[1]};
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // to properly interrupt and return
        return;
      }
      System.out.println(participant.name() + " is searching " + participant.maze());
    }
  }
}

public class MazeRunner {

  public static void main(String[] args) {

    Maze maze = new Maze();
    Participant adam = new Participant("Adam", "Grace", maze, new int[]{3, 3});
    Participant grace = new Participant("Grace", "Adam", maze, new int[]{1, 1});

    System.out.println(maze);

    var executor = Executors.newCachedThreadPool();
    var adamsResult = executor.submit(new ParticipantThread(adam));
    var gracesResult = executor.submit(new ParticipantThread(grace));

    while (!adamsResult.isDone() && !gracesResult.isDone()) {
      try {
        Thread.sleep(1_000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    if (adamsResult.isDone()) {
      gracesResult.cancel(true);
    } else if (gracesResult.isDone()) {
      adamsResult.cancel(true);
    }
    executor.shutdown();

    try {
      System.out.println("Adam result: " + adamsResult.get(10, TimeUnit.SECONDS));
      System.out.println("Grace result: " + gracesResult.get(10, TimeUnit.SECONDS));

      if(executor.awaitTermination(10, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException | TimeoutException | ExecutionException e) {
      e.printStackTrace();
    }
  }
}

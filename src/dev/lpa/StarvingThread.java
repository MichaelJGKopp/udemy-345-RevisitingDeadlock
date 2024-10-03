package dev.lpa;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StarvingThread {

  private static final Lock lock = new ReentrantLock(true);

  public static void main(String[] args) {

    Callable<Boolean> thread = () -> {
      String threadName = Thread.currentThread().getName();
      int threadNo = Integer.parseInt(
        threadName.replaceAll(".*-[1-9]*-.*-", ""));
      boolean keepGoing = true;

      String[] colors = {"\u001B[30m", "\u001B[31m", "\u001B[32m", "\u001B[33m", "\u001B[34m",
        "\u001B[35m", "\u001B[36m", "\u001B[37m"};
      String color = colors[threadNo % colors.length];

      while (keepGoing) {
        lock.lock();
        try {
          System.out.printf("%s acquired the lock.%n", color + threadNo);
          TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
          System.out.printf("Shutting down %d%n", threadNo);
          Thread.currentThread().interrupt();
          return false; // needed to stop the thread when shutdownNow is called
        } finally {
          lock.unlock();
        }
      }
      return true;
    };

    ExecutorService executor = Executors.newFixedThreadPool(3);
    try {
      List<Future<Boolean>> futures = executor.invokeAll(
        List.of(thread, thread, thread),
        10, TimeUnit.SECONDS); // tasks are in infinite loop, so timeout needed
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    executor.shutdownNow(); // stops all activity of running threads since they loop infinitely.
    // could have also called cancel on each future

  }
}

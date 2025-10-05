package com.unravel.part3ConcurrencyProblem;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.unravel.part3ConcurrencyProblem.Constants.TOTAL_TASKS;

public class LogProcessingApp {
    public static void main(String[] args) throws InterruptedException {
        LogProcessor processor = new LogProcessor();

        ExecutorService producerExecutor = Executors.newSingleThreadExecutor();
        producerExecutor.submit(new Producer(processor));

        int numConsumers = 1;
        CountDownLatch latch = new CountDownLatch(TOTAL_TASKS);  // Matches the number of tasks produced
        ExecutorService consumerExecutor = Executors.newFixedThreadPool(numConsumers);
        for (int i = 0; i < numConsumers; i++) {
            consumerExecutor.submit(new Consumer(processor, latch));
        }

        producerExecutor.shutdown();
        producerExecutor.awaitTermination(10, TimeUnit.SECONDS);

        latch.await();
        consumerExecutor.shutdownNow();
    }
}

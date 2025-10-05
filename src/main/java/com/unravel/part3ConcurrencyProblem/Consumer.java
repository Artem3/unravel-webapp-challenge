package com.unravel.part3ConcurrencyProblem;

import lombok.AllArgsConstructor;

import java.util.concurrent.CountDownLatch;

@AllArgsConstructor
class Consumer implements Runnable {
    private LogProcessor processor;
    private CountDownLatch latch;

    @Override
    public void run() {
        try {
            while (true) {
                LogTask task = processor.consumeLog();

                long now = System.nanoTime();
                long ageNs = now - task.getCreatedAtNanos();

                String agingNote = ageNs > Constants.AGE_THRESHOLD_NS ? " (AGED ↑)" : "";

                System.out.printf("%s | %s processed %s | age=%.1f ms%s%n",
                        java.time.LocalTime.now(),
                        Thread.currentThread().getName(),
                        task.getMessage(),
                        ageNs / Constants.NS_TO_MS,
                        agingNote);

                Thread.sleep(1);  // Simulate processing delay to allow aging
                latch.countDown();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

package com.unravel.part3ConcurrencyProblem;

import java.util.concurrent.PriorityBlockingQueue;

public class LogProcessor {
    private final PriorityBlockingQueue<LogTask> queue = new PriorityBlockingQueue<>();

    public void produceLog(LogTask task) {
        queue.put(task);
    }

    public LogTask consumeLog() throws InterruptedException {
        return queue.take();
    }
}
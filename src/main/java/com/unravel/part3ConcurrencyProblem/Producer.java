package com.unravel.part3ConcurrencyProblem;

import lombok.AllArgsConstructor;

import java.util.Random;

@AllArgsConstructor
class Producer implements Runnable {
    private LogProcessor processor;
    private final Random random = new Random();

    @Override
    public void run() {
        for (int i = 0; i < Constants.TOTAL_TASKS; i++) {
            TaskPriority priority = getRandomPriority();
            LogTask task = new LogTask(priority, priority + " log " + i, System.nanoTime());
            processor.produceLog(task);
        }
    }

    private TaskPriority getRandomPriority() {
        return TaskPriority.values()[random.nextInt(TaskPriority.values().length)];
    }
}

package com.unravel.part3ConcurrencyProblem;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class LogTask implements Comparable<LogTask> {

    private TaskPriority priority;
    private String message;
    private long createdAtNanos;

    @Override
    public int compareTo(LogTask other) {
        // The smaller the ordinal, the higher the priority
        int priorityDiff = this.priority.ordinal() - other.priority.ordinal();

        // Calculate "aging" — the longer it sits, the higher the chance of moving up
        long now = System.nanoTime();
        long ageThis = now - this.createdAtNanos;
        long ageOther = now - other.createdAtNanos;

        // If priorities are equal, the older task will be moved up
        if (priorityDiff == 0) {
            return Long.compare(ageOther, ageThis);
        }

        // If a low-priority task has been sitting around for too long, we "artificially" raise it
        if (priorityDiff > 0 && ageThis > Constants.AGE_THRESHOLD_NS) {
            return priorityDiff - 1;
        }

        return priorityDiff;
    }
}
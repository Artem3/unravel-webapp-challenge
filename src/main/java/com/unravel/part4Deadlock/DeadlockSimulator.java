package com.unravel.part4Deadlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class DeadlockSimulator {
    final ReentrantLock lock1 = new ReentrantLock();
    final ReentrantLock lock2 = new ReentrantLock();

    public void method1() {
        // Acquire locks in consistent order (lock1 before lock2)
        if (tryAcquireLocks(lock1, lock2)) {
            try {
                System.out.println("Method1: Acquired lock1 and lock2");
            } finally {
                lock2.unlock();
                lock1.unlock();
            }
        } else {
            System.out.println("Method1: Failed to acquire locks, backing off");
            // In production, add retry logic or handle failure gracefully
        }
    }

    public void method2() {
        // Same consistent order to avoid deadlock
        if (tryAcquireLocks(lock1, lock2)) {
            try {
                System.out.println("Method2: Acquired lock1 and lock2");
            } finally {
                lock2.unlock();
                lock1.unlock();
            }
        } else {
            System.out.println("Method2: Failed to acquire locks, backing off");
            // In production, add retry logic or handle failure gracefully
        }
    }

    private boolean tryAcquireLocks(ReentrantLock first, ReentrantLock second) {
        boolean acquiredFirst = false;
        boolean acquiredSecond = false;
        try {
            // Try to lock with timeout to avoid an indefinite wait
            acquiredFirst = first.tryLock(100, TimeUnit.MILLISECONDS);
            if (acquiredFirst) {
                acquiredSecond = second.tryLock(100, TimeUnit.MILLISECONDS);
            }
            return acquiredFirst && acquiredSecond;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            // Release partial locks to prevent resource hold
            if (acquiredFirst && !acquiredSecond) {
                first.unlock();
            }
        }
    }

    public static void main(String[] args) {
        DeadlockSimulator simulator = new DeadlockSimulator();
        Thread t1 = new Thread(simulator::method1);
        Thread t2 = new Thread(simulator::method2);
        t1.start();
        t2.start();
    }
}
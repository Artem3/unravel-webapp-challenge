package com.unravel.part4Deadlock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class DeadlockSimulatorTest {

    private DeadlockSimulator simulator;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        simulator = new DeadlockSimulator();
        System.setOut(new PrintStream(outputStreamCaptor));  // Capture System.out for assertions
    }

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    public void testMethod1SuccessfulAcquisition() {
        // Test single thread successful lock acquisition
        simulator.method1();
        String output = outputStreamCaptor.toString().trim();
        assertEquals("Method1: Acquired lock1 and lock2", output);
    }

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    public void testMethod2SuccessfulAcquisition() {
        // Test single thread successful lock acquisition
        simulator.method2();
        String output = outputStreamCaptor.toString().trim();
        assertEquals("Method2: Acquired lock1 and lock2", output);
    }

    @Test
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    public void testConcurrentExecutionNoDeadlock() throws InterruptedException {
        // Test concurrent calls to ensure no deadlock and eventual completion
        CountDownLatch latch = new CountDownLatch(2);
        Thread t1 = new Thread(() -> {
            simulator.method1();
            latch.countDown();
        });
        Thread t2 = new Thread(() -> {
            simulator.method2();
            latch.countDown();
        });
        t1.start();
        t2.start();
        assertTrue(latch.await(500, TimeUnit.MILLISECONDS));
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains("Method1: Acquired lock1 and lock2") || output.contains("Method1: Failed to acquire locks, backing off"));
        assertTrue(output.contains("Method2: Acquired lock1 and lock2") || output.contains("Method2: Failed to acquire locks, backing off"));
    }

    @Test
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    public void testFailedAcquisitionDueToContention() throws InterruptedException {
        // Simulate contention: hold locks in one thread, attempt acquisition in another
        simulator.lock1.lock();
        simulator.lock2.lock();
        try {
            Thread t = new Thread(simulator::method1);
            t.start();
            t.join(200);  // Wait for attempt
            String output = outputStreamCaptor.toString().trim();
            assertEquals("Method1: Failed to acquire locks, backing off", output);
        } finally {
            simulator.lock2.unlock();
            simulator.lock1.unlock();
        }
    }

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    public void testInterruptDuringAcquisition() throws InterruptedException {
        // Test handling of interruption during tryLock
        Thread t = new Thread(simulator::method1);
        t.start();
        t.interrupt();  // Interrupt immediately
        t.join();
        assertTrue(t.isInterrupted());  // Verify interrupt flag set
        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.isEmpty() || output.contains("Failed to acquire locks"));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS)
    public void testHighContentionMultipleThreads() throws InterruptedException {
        // Stress test with multiple threads to ensure no deadlock under load
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    if (index % 2 == 0) {
                        simulator.method1();
                    } else {
                        simulator.method2();
                    }
                    endLatch.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        startLatch.countDown();  // Start all threads simultaneously
        assertTrue(endLatch.await(1000, TimeUnit.MILLISECONDS));  // Ensure all complete without deadlock
    }
}
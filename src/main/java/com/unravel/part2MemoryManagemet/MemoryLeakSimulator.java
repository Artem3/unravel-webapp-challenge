package com.unravel.part2MemoryManagemet;

public class MemoryLeakSimulator {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            MemoryManager.addSessionData("sess-" + i);
            System.out.println("added " + i);
            Thread.sleep(20);
        }
        System.out.println("finished adding large SessionData.");
    }
}

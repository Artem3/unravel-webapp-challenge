# Solutions and rationale for Unravel webapp challenges

---

## 1. Session Management

### Problem

The initial implementation uses a local `ConcurrentHashMap` for sessions, leading to incomplete thread safety, lack of
synchronization across microservices, weak exception handling, and performance issues.

### Solution

Integrated Spring Session with **Redis** for distributed session management.

* Add dependencies to `pom.xml`
* Configure `application.properties`
* Enable with `@EnableRedisHttpSession`
* Refactor `SessionManager` class to use `HttpSession`
* Create `CustomSessionException` provides session-specific semantic clarity, simplifying targeted handling, logging,
  and
  future extension

### Why This Approach

I chose Redis because it provides atomic operations and centralized storage out of the box. It's ensuring thread safety
and consistency across microservices without custom synchronization. Redis offers eviction (TTL) for memory
efficiency.  
A possible alternative to Redis in high-load applications is stateless session storage using **JWT tokens**. However, in
this specific case, that option would require significantly more refactoring and the writing of additional logic.
Alternatives like JWT are stateless but less suitable for mutable shared data.

### Further Improvements

* Implement Redis monitoring with Prometheus.
* Explore session compression for reduced network traffic.

---

## 2. Memory Management

### Problem

Sporadic memory leaks under high loads from static HashMap storing 10MB byte arrays per session without eviction,
expiration, causing OOM error.

**_Memory Leak Analysis_**  
To reproduce the memory leak issue, I created a new class called `MemoryLeakSimulator` that invokes the problematic code
in a loop 1000 times. At each step I output a debug message to the console with a counter.
Using beautiful build-in IntelliJ IDEA Profiler, I observed a rapid increase in memory usage until an `OutOfMemoryError`
occurred at step ~600.
The profiler results showed a large number of `byte[]` objects allocated inside `MemoryManager.addSessionData`. These
arrays were retained by the static HashMap `largeSessionData`, which never released them.
This confirmed the memory leak source.

### Solution

I selected a **Caffeine** library (Guava Cache as an option) for its high-performance, configurable caching with
built-in eviction strategies to address memory leaks.

* Add Caffeine dependency to `pom.xml`.
* Refactor `MemoryManager` to use a Caffeine cache instance.
* Configured with a `maximum size` limit, access-based `expiration`, and `weak keys`.
* Add SLF4J logging

### Why This Approach

I see that there are three approaches to solving this problem.

**1. Low-level (сustom)**. Implement manual eviction using `WeakHashMap<String, byte[]>` for GC-eligible keys and a
`ScheduledExecutorService` for periodic cleanup (e.g., scan and remove inactive entries every X sec/min). The advantages
of this approach are: full control and flexibility, but disadvantages are error-prone, requires custom logic for size
limits/expiration, low-maintenance apps, risking race conditions.

**2. Library-based (Caffeine/Guava)** approach. I believe the approach I chose is the most appropriate for this
situation. Caffeine provides efficient, configurable caching over plain HashMap, preventing leaks via size limits and
expiration without a manual cleanup. Weak keys enable GC integration for sporadic issues. Reduces pressure by evicting
unused data, ensuring scalability without OOM.   
After the implementation, I ran tests again to make sure the solution was correct, and the results showed that
`largeSessionData` can now handle any number of cycles (1K, 10K, 100K) without memory errors.

**3. Enterprise.**
Here I must say I see some confusion or inconsistency. The second task requires fixing an OOM, and the hashmap code is
provided. However, it's also clear that this class is part of the user session management mechanism, which is a
higher-level structure, and the approaches to solving this problem are different.

Therefore, if we're fixing specific memory leaks, I'll choose the library-based approach, but if we're fixing sessions,
I've already proposed a solution in the first task, namely Redis. Offload to Redis, storing session data
as hashes with TTL expiration and invalidation.

### Further Improvements

* Integrate Micrometer for real-time cache metrics (hit/miss rates, eviction counts) to enable proactive monitoring.
* Add comprehensive integration tests with Testcontainers simulating multi-instance deployments

---

## 3. Producer-Consumer Problem

### Problem

Implement the Producer-Consumer pattern using threads to manage concurrent, multi-priority tasks. The solution must
feature a dynamic priority mechanism to ensure critical tasks are processed promptly while preventing starvation of
low-priority tasks.

### Solution

I implemented a prioritized producer-consumer system using a PriorityBlockingQueue with a time-dependent aging
mechanism to prevent starvation of low-priority tasks, managed by thread pools for scalability.

* Create the `LogTask` class implementing `Comparable<LogTask>` to handle priority comparison with aging adjustment
  based on creation time.
* Modify the `LogProcessor` class using `PriorityBlockingQueue` for thread-safe task storage and retrieval.
* Modify the `Producer` as a `Runnable` that generates tasks with random priorities and adds them to the queue.
* Modify the `Consumer` as a `Runnable` that consumes tasks, logs processing details including age, simulates work with
  a short sleep, and counts down a latch.
* In `LogProcessingApp`, set up `ExecutorService` for a single producer and multiple consumers, use `CountDownLatch` to
  wait for all tasks, and shut down executors properly.

### Why This Approach

This approach was selected because PriorityBlockingQueue provides built-in thread-safety and efficient priority-based
retrieval, eliminating the need for manual synchronization while handling concurrency effectively. The time-dependent
aging mechanism in the comparator dynamically promotes lower-priority tasks after a threshold, balancing critical task
prioritization with starvation prevention in a simple, low-overhead way. Using thread pools via ExecutorService enhances
scalability for high loads by managing resource allocation better than direct thread creation, and CountDownLatch
ensures reliable coordination of task completion without busy-waiting, aligning with Java's concurrent utilities for
robustness and performance.

### Further Improvements

* Switch to fixed priorities with creation timestamp as a stable tie-breaker in compareTo to avoid inconsistent ordering
  from time-dependent comparisons.
* Implement a custom scheduler thread that periodically scans and re-prioritizes aged tasks by removing and re-inserting
  them into the queue.
* Use multiple queues per priority level with weighted round-robin selection for more predictable anti-starvation.

---
# Solutions and rationale for Unravel webapp challenges

---

## 1. Session Manager

### Problem

The initial implementation uses a local `ConcurrentHashMap` for sessions, leading to incomplete thread safety, lack of
synchronization across microservices, weak exception handling and performance issues.

### Solution

Integrated Spring Session with Redis for distributed session management.

* Add dependencies to `pom.xml`
* Configure `application.properties`
* Enable with `@EnableRedisHttpSession`
* Refactor `SessionManager` class to use `HttpSession`

### Why This Approach

Redis provides atomic operations and centralized storage out of the box. It's ensuring thread safety and consistency
across microservices
without custom synchronization. Redis offers eviction (TTL) for memory efficiency.  
A possible alternative to Redis in high-load applications is stateless session storage using JWT tokens. However, in
this specific case, that option would require significantly more refactoring and the writing of additional logic.
Alternatives like JWT are stateless but less suitable for mutable shared data.

### Further Improvements

* Implement Redis monitoring with Prometheus.
* Explore session compression for reduced network traffic.

---


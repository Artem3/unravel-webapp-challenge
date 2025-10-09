![logo](logo.png)

# Backend Challenge Solutions

This project contains solutions for the Unravel Backend Developer Challenge.
The implementation is organized into dedicated packages within the source code.  
The justification and detailed technical solutions for each task are described in
the [Project Documentation](#project-documentation) sections below.

## Getting Started

### Prerequisites

To build and run the project, you need the following:

* [Oracle JDK 17 Downloads](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or higher.
* [Maven](https://maven.apache.org/install.html) for building and managing the project dependencies.
* [Docker Compose](https://docs.docker.com/compose/install/) required for MySql database.

### Building and Running

The project uses the Maven Wrapper (mvnw or mvnw.cmd), so a global Maven installation is optional.

1. For **Session Management,** no specific runner was allocated. The solution was achieved purely through the
   inclusion of necessary library dependencies.


2. For running the second task, **Memory Management,** please use
   the [MemoryLeakSimulator.java](src/main/java/com/unravel/part2MemoryManagemet/MemoryLeakSimulator.java)


3. To run a **Producer-Consumer** solution, please use
   the [LogProcessingApp.java](src/main/java/com/unravel/part3ConcurrencyProblem/LogProcessingApp.java)


4. For running the **Deadlock** solution, please use
   the [DeadlockSimulator.java](src/main/java/com/unravel/part4Deadlock/DeadlockSimulator.java)


5. To be able to observe logs of **Database Connection Pooling** solution, you need two things. The first is to run the
   application main method.
    ```bash
    mvnw spring-boot:run
    ``` 
   This will allow the scheduler to start a counter automatically trigger the `monitorPool()` method within the
   `DatabaseManager` class.  
   The second is having MySql db up and running. To simplify this, I've added a docker-compose file to the root
   of the project which will automatically download an image and run the container with MySql.
   If you already have a MySql instance running, you can disable `spring.docker.compose.enabled=false` property in the
   [application.properties](src/main/resources/application.properties)

## Project Documentation

* [Session Management problem](SOLUTION.md#1-session-management)
* [Memory Management issue](SOLUTION.md#2-memory-management)
* [Producer-Consumer problem](SOLUTION.md#3-producer-consumer-problem)
* [Deadlock](SOLUTION.md#4-Deadlock)
* [Database Connection pooling](SOLUTION.md#5-database-connection-pooling)
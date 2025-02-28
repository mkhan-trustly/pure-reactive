## Overview

Uses Spring Webflux to demonstrate writing reactive code, the application fetches traffic messages from
Sveriges Radio [Open API](https://api.sr.se/api/documentation/v2/index.html).

The endpoint we are using here is [fetch traffic messages](https://api.sr.se/api/v2/traffic/messages?format=json).

## What we learn here
We learn not to use block in the code, it would demean the purpose of writing reactive code. I have often 
encountered code that uses reactive library with a lot of blocks in the code so this has motivated me to 
write the article & demo code. 

## Do you really require Reactive code? 
This is the question that you need to ask before starting writing reactive code since it may increase the application's
complexity.

I would not use Reactive code in the following types of applications:
- Traditional CRUD-based applications (Most enterprise apps, banking, admin portals)
- Database-heavy apps with relational transactions (JPA/Hibernate doesn’t work well in reactive mode)
- Applications with synchronous business logic (Easier to read, maintain, and debug with imperative style)
- Simple REST APIs where blocking I/O is not a bottleneck.

On the other hand Reactive programming shines in applications that require 
high concurrency, low latency, and efficient resource utilization. Some key scenarios include:

- Streaming data processing
  - Real-time stock market feeds
  - Live analytics dashboards
  - Continuous sensor data processing (IoT)
  
- High Throughput APIs with many concurrent requests
  - Public APIs handling thousands of concurrent users
  - Large-scale e-commerce systems (handling many product searches, real-time inventory updates)
  
- Microservices with heavy asynchronous communication
  - When using event-driven architecture (Kafka, RabbitMQ)
  - Chained API calls where blocking would create bottlenecks
  
- Chat applications & notifications
  - WebSockets-based real-time messaging
  - Push notifications for mobile/web apps
  
- Cloud-Native & Serverless applications
  - Function-as-a-Service (FaaS) workloads where you pay per execution
  - Applications that scale horizontally based on demand
  
- Database Interaction with Reactive drivers
  - MongoDB (Reactive Streams API)
  - Redis (Lettuce reactive client)
  - Cassandra (Reactive CQL)

If you still are here and want to still use reactive approach then we can move to the next section.

## Deeper dive into avoiding .block()

### Why using .block() is often wrong
- Blocks Threads: 
.block() converts a non-blocking operation (like a Mono or Flux) into a blocking one by waiting for 
the result synchronously. This blocks the calling thread, which goes against the very idea of reactive programming.
- Decreased scalability: 
Reactive programming shines when handling high-concurrency scenarios with limited threads(e.g., Netty in Spring WebFlux).
Blocking a thread with .block() reduces the thread pool’s efficiency, limiting scalability.
- Thread context issues: 
In reactive programming, the thread context is often carefully managed (e.g., using event loops or specific schedulers).
Blocking a thread with .block() can disrupt this context and lead to unpredictable behavior.
- Mixed paradigm confusion: 
Using .block() often indicates that you are mixing reactive and imperative code, which can lead to messy, 
hard-to-maintain applications.

### When is .block() acceptable?
There are limited scenarios where .block() can be justified:
- Bootstrapping or Initialization: When starting the application (e.g., to pre-fetch configuration or validate something 
at startup), using .block() in a main method or initialization logic might be fine since it happens in a controlled,
one-time context.

```
String result = WebClient.create("http://example.com")
                         .get()
                         .retrieve()
                         .bodyToMono(String.class)
                         .block();
```

- Interfacing with Blocking APIs: If you are working with an API or framework that is not reactive and requires 
blocking, you may need to call .block(). However, it is better to find or create an adapter to bridge reactive and blocking code.
- Testing: In unit tests, .block() can be used to get the result of a reactive operation synchronously, but even then, 
tools like StepVerifier (from Project Reactor) are preferred.

### How to avoid .block()?

- Use Reactive Composition: Compose your reactive streams using operators like flatMap, zip, and map, rather than blocking.

Example:
Instead of this (blocking):
```
String result = someReactiveCall().block(); 

//alternatively

someReactiveCall()
    .flatMap(data -> anotherReactiveCall(data))
    .subscribe(result -> System.out.println("Result: " + result));
```

- Use Mono or Flux as return types
In reactive programming, the calling code should handle the reactive Mono or Flux chain instead of converting it 
to a blocking value.

```
@RestController
public class ReactiveController {

    private final WebClient webClient = WebClient.create();

    @GetMapping("/reactive-endpoint")
    public Mono<String> getReactiveData() {
        return webClient.get()
                        .uri("http://example.com/api")
                        .retrieve()
                        .bodyToMono(String.class);
    }
}
```
Here, the Mono<String> is returned directly, and the WebFlux runtime will handle it asynchronously without blocking.

- Use Schedulers for Integration with legacy code

If you are working with blocking libraries (like JDBC or legacy APIs), wrap the blocking code in a reactive scheduler.

```
Mono.fromCallable(() -> {
    // Blocking code here
    return blockingApiCall();
}).subscribeOn(Schedulers.boundedElastic())
  .subscribe(result -> System.out.println("Processed result: " + result));
```
This approach ensures that blocking code runs on a separate thread pool, keeping the event loop thread free for other tasks.

### Use of multiple operators

```
@GetMapping("/chained-data")
public Flux<String> getChainedData() {
    return webClient.get()
                    .uri("/data")
                    .retrieve()
                    .bodyToFlux(String.class)
                    .filter(data -> data.length() > 5)
                    .map(String::toLowerCase)
                    .distinct();
}
```

Handling multiple API Calls

```
@GetMapping("/combine-and-filter")
public Mono<List<String>> getCombinedAndFilteredData() {
    Mono<List<String>> api1 = webClient.get()
                                       .uri("/api1")
                                       .retrieve()
                                       .bodyToFlux(String.class)
                                       .filter(data -> data.startsWith("A"))
                                       .collectList();

    Mono<List<String>> api2 = webClient.get()
                                       .uri("/api2")
                                       .retrieve()
                                       .bodyToFlux(String.class)
                                       .filter(data -> data.endsWith("Z"))
                                       .collectList();
                                       
    return Mono.zip(api1, api2)
               .map(tuple -> {
                   List<String> combined = new ArrayList<>(tuple.getT1());
                   combined.addAll(tuple.getT2());
                   return combined;
               });
}
```

## CompletableFuture
TBD

```
CompletableFuture.supplyAsync(() -> {
    // Perform async computation
    return "Result";
}).thenApply(result -> {
    // Process the result
    return result + " processed";
}).exceptionally(ex -> {
    // Handle exceptions
    return "Error occurred";
});
```

## RestClient vs WebClient
RestClient is the latest and recommended way to call REST APIs as of Spring Framework 6 and Spring Boot 3.
But it is Blocking and has no reactive support, the api style is modern.

Use WebClient for reactive projects, this is asynchronous.

RestTemplate? Time to upgrade if you are still using it.

## Object wrapper
Important to ignore fields that we don't want to map, see the bean configuration.
As a bonus I have added brief description on the different configurations.


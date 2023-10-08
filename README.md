# retry-java

[![tests](https://github.com/akornatskyy/retry-java/actions/workflows/tests.yaml/badge.svg)](https://github.com/akornatskyy/retry-java/actions/workflows/tests.yaml)
[![maven central](https://img.shields.io/maven-central/v/io.github.akornatskyy/retry.svg)](https://search.maven.org/search?q=g:%22io.github.akornatskyy%22%20AND%20a:%22retry%22)

Repeat failed call.

## Usage

Initialize the retry options to max 3 calls and a fixed backoff of 500 ms.

```java
RetryOptions options = RetryOptions.builder()
    .max(3)
    .backoff(new FixedBackoff(Duration.ofMillis(500)))
    .build();
```

Initialize the retry options with ±10% jitter backoff of 2 seconds.

```java
RetryOptions options = RetryOptions.builder()
    .backoff(new JitterBackoff(Duration.ofSeconds(2), 0.1))
    .build();
```

Initialize the retry options with exponential backoff starting at 1 second and
growing by 1.5 with ±25% jitter.

```java
RetryOptions options = RetryOptions.builder()
    .backoff(
        ExpBackoff.builder()
            .initial(Duration.ofSeconds(1))
            .multiplier(1.5)
            .factor(0.25)
            .build())
    .build();
```

### Retry Runnable

```java
RetryRunnable.run(() -> handler.operation(), options);
```

[Runnable](https://docs.oracle.com/javase/7/docs/api/java/lang/Runnable.html) 
with predicate (repeats call for `MyRuntimeException` only):

```java
RetryRunnable.run(
    () -> handler.operation(100),
    (ex) -> ex instanceof MyRuntimeException,
    options);
```

### Retry Supplier

```java
Response response = RetrySupplier.get(() -> handler.operation(), options);
```

[Supplier](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html)
with predicate for response value:

```java
Response response = RetrySupplier.get(
    () -> handler.operation(100),
    (r, ex) -> r != null && r.getStatusCode() != 200,
    options);
```

### Retry Throwing Supplier

```java
Response response = RetryThrowingSupplier.get(
    () -> handler.operation(), options);
```

### Retry Async Supplier

Initialize `ScheduledExecutorService` (used to schedule retry delay):

```java
ScheduledExecutorService scheduledExecutor = Executors
    .newSingleThreadScheduledExecutor();
```

```java
CompletableFuture<Response> future = RetryAsyncSupplier.get(
    scheduledExecutor,
    () -> handler.operation(100),
    options);
```

`Supplier<CompletableFuture<T>>` with predicate for response value:

```java
CompletableFuture<Response> future = RetryAsyncSupplier.get(
    scheduledExecutor,
    () -> handler.operation(),
    (r, ex) -> r != null && r.getStatusCode() != 200,
    options);
```

## Install

Add as a maven dependency:

```xml
<dependency>
  <groupId>io.github.akornatskyy</groupId>
  <artifactId>retry</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Release

```sh
mvn versions:set -DnewVersion=1.X.0
mvn -P release clean deploy
```
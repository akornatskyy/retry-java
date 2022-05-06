# retry-java

[![tests](https://github.com/akornatskyy/retry-java/actions/workflows/tests.yaml/badge.svg)](https://github.com/akornatskyy/retry-java/actions/workflows/tests.yaml)

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
    (r, ex) -> r.getStatusCode() != 200,
    options);
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
    (r, ex) -> r.getStatusCode() != 200,
    options);
```

## Install

Add as a maven dependency:

```xml
<dependency>
  <groupId>com.github.akornatskyy</groupId>
  <artifactId>retry</artifactId>
  <version>1.0.0</version>
</dependency>
```

or use a snapshot from Sonatype:

```xml
<dependency>
  <groupId>io.github.akornatskyy</groupId>
  <artifactId>retry</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

```xml
<repositories>
  <repository>
    <id>snapshots</id>
    <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

## Release

```sh
mvn versions:set -DnewVersion=1.0.0
mvn -P release clean deploy
```
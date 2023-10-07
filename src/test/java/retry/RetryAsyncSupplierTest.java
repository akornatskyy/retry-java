package retry;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RetryAsyncSupplierTest {
  private final static RetryOptions OPTIONS = RetryOptions.builder()
      .max(3)
      .backoff(new FixedBackoff(Duration.ofMillis(0)))
      .build();
  private final static ScheduledExecutorService SCHEDULED_EXECUTOR = Executors
      .newSingleThreadScheduledExecutor();

  @Test
  void get() {
    Supplier<CompletableFuture<Integer>> supplier = Mockito.mock(Supplier.class);
    Mockito.when(supplier.get())
        .thenReturn(CompletableFuture.completedFuture(100));

    int result = RetryAsyncSupplier.get(
            SCHEDULED_EXECUTOR,
            supplier,
            OPTIONS)
        .join();

    Assertions.assertEquals(100, result);
    Mockito.verify(supplier, Mockito.times(1)).get();
  }

  @Test
  void fails() {
    Supplier<CompletableFuture<Boolean>> supplier = Mockito.mock(Supplier.class);
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    future.completeExceptionally(new RuntimeException());
    Mockito.when(supplier.get()).thenReturn(future);

    Assertions.assertThrows(
        RuntimeException.class,
        () -> RetryAsyncSupplier.get(
            SCHEDULED_EXECUTOR,
            supplier,
            OPTIONS).join());

    Mockito.verify(supplier, Mockito.times(3)).get();
  }

  @Test
  void predicateIgnoresError() {
    Supplier<CompletableFuture<Boolean>> supplier = Mockito.mock(Supplier.class);
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    future.completeExceptionally(new RuntimeException());
    Mockito.when(supplier.get()).thenReturn(future);

    Assertions.assertThrows(
        CompletionException.class,
        () -> RetryAsyncSupplier.get(
            SCHEDULED_EXECUTOR,
            supplier,
            (result, ex) -> ex instanceof IllegalArgumentException,
            OPTIONS).join());

    Mockito.verify(supplier, Mockito.times(1)).get();
  }

  @Test
  void predicateRetriesError() {
    Supplier<CompletableFuture<Boolean>> supplier = Mockito.mock(Supplier.class);
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    future.completeExceptionally(new IllegalArgumentException());
    Mockito.when(supplier.get()).thenReturn(future);

    Assertions.assertThrows(
        CompletionException.class,
        () -> RetryAsyncSupplier.get(
            SCHEDULED_EXECUTOR,
            supplier,
            (result, ex) -> ex instanceof IllegalArgumentException,
            OPTIONS).join());

    Mockito.verify(supplier, Mockito.times(3)).get();
  }

  @Test
  void predicateRetriesCompletionError() {
    Supplier<CompletableFuture<Boolean>> supplier = Mockito.mock(Supplier.class);
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    future.completeExceptionally(
        new CompletionException(new IllegalArgumentException()));
    Mockito.when(supplier.get()).thenReturn(future);

    Assertions.assertThrows(
        CompletionException.class,
        () -> RetryAsyncSupplier.get(
            SCHEDULED_EXECUTOR,
            supplier,
            (result, ex) -> ex instanceof IllegalArgumentException,
            OPTIONS).join());

    Mockito.verify(supplier, Mockito.times(3)).get();
  }

  @Test
  void predicateRetriesResult() {
    Supplier<CompletableFuture<Integer>> supplier = Mockito.mock(Supplier.class);
    Mockito.when(supplier.get())
        .thenReturn(CompletableFuture.completedFuture(100));

    int r = RetryAsyncSupplier.get(
            SCHEDULED_EXECUTOR,
            supplier,
            (result, ex) -> result != 200,
            OPTIONS)
        .join();

    Assertions.assertEquals(100, r);
    Mockito.verify(supplier, Mockito.times(3)).get();
  }
}
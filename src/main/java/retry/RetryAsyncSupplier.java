package retry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * The type Retry async supplier.
 */
public final class RetryAsyncSupplier {
  private RetryAsyncSupplier() {
  }

  /**
   * Get completable future.
   *
   * @param <T>             the type parameter
   * @param executorService the executor service
   * @param supplier        the supplier
   * @param options         the options
   * @return the completable future
   */
  public static <T> CompletableFuture<T> get(
      ScheduledExecutorService executorService,
      Supplier<CompletableFuture<T>> supplier,
      RetryOptions options) {
    return get(executorService, supplier, null, options);
  }

  /**
   * Get completable future.
   *
   * @param <T>             the type parameter
   * @param executorService the executor service
   * @param supplier        the supplier
   * @param predicate       the predicate
   * @param options         the options
   * @return the completable future
   */
  public static <T> CompletableFuture<T> get(
      ScheduledExecutorService executorService,
      Supplier<CompletableFuture<T>> supplier,
      BiPredicate<T, Throwable> predicate,
      RetryOptions options) {
    CompletableFuture<T> future = new CompletableFuture<>();
    Runnable runnable = new Runnable() {
      int attempt;
      long delay;

      @Override
      public void run() {
        supplier.get().whenComplete((result, throwable) -> {
          boolean retry =
              predicate != null
              ? predicate
                  .test(result,
                        throwable instanceof CompletionException
                        || throwable instanceof ExecutionException
                        ? throwable.getCause() : throwable)
              : throwable != null;
          if (retry && ++attempt < options.getMax()) {
            delay = options.getBackoff().next(delay);
            executorService.schedule(this, delay, TimeUnit.MILLISECONDS);
            return;
          }

          if (throwable == null) {
            future.complete(result);
          } else {
            future.completeExceptionally(throwable);
          }
        });
      }
    };
    runnable.run();
    return future;
  }
}

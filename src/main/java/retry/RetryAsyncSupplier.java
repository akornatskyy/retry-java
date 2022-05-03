package retry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public final class RetryAsyncSupplier {
  private RetryAsyncSupplier() {
  }

  public static <T> CompletableFuture<T> get(
      ScheduledExecutorService executorService,
      Supplier<CompletableFuture<T>> supplier,
      RetryOptions options) {
    return get(executorService, supplier, null, options);
  }

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

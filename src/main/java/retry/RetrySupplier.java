package retry;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * The type Retry supplier.
 */
public final class RetrySupplier {
  private RetrySupplier() {
  }

  /**
   * Gets a result.
   *
   * @param <T>      the type parameter
   * @param supplier the supplier
   * @param options  the options
   * @return a result
   */
  public static <T> T get(Supplier<T> supplier, RetryOptions options) {
    return get(supplier, null, options);
  }

  /**
   * Gets a result.
   *
   * @param <T>       the type parameter
   * @param supplier  the supplier
   * @param predicate the predicate
   * @param options   the options
   * @return a result
   */
  public static <T> T get(
      Supplier<T> supplier,
      BiPredicate<T, RuntimeException> predicate,
      RetryOptions options) {
    long delay = 0;
    int attempt = 0;
    for (; ; ) {
      T result = null;
      RuntimeException exception = null;
      boolean retry;
      try {
        result = supplier.get();
        retry = predicate != null && predicate.test(result, null);
      } catch (RuntimeException ex) {
        exception = ex;
        retry = predicate == null || predicate.test(null, ex);
      }

      if (retry && ++attempt < options.getMax()) {
        delay = options.getBackoff().next(delay);
        try {
          Thread.sleep(delay);
        } catch (InterruptedException ex) {
          throw new IllegalStateException(ex);
        }

        continue;
      }

      if (exception == null) {
        return result;
      } else {
        throw exception;
      }
    }
  }
}

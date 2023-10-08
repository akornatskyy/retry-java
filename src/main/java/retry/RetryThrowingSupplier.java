package retry;

import java.util.function.BiPredicate;

/**
 * The type Retry throwing supplier.
 */
public final class RetryThrowingSupplier {
  private RetryThrowingSupplier() {
  }

  /**
   * Gets a result.
   *
   * @param <T>      the type parameter
   * @param supplier the supplier
   * @param options  the options
   * @return the t
   */
  public static <T> T get(ThrowingSupplier<T> supplier, RetryOptions options)
      throws Exception {
    return get(supplier, null, options);
  }

  /**
   * Gets the result.
   *
   * @param <T>       the type parameter
   * @param supplier  the supplier
   * @param predicate the predicate
   * @param options   the options
   * @return the t
   */
  public static <T> T get(
      ThrowingSupplier<T> supplier,
      BiPredicate<T, Exception> predicate,
      RetryOptions options) throws Exception {
    long delay = 0;
    int attempt = 0;
    for (; ; ) {
      T result = null;
      Exception exception = null;
      boolean retry;
      try {
        result = supplier.get();
        retry = predicate != null && predicate.test(result, null);
      } catch (Exception ex) {
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

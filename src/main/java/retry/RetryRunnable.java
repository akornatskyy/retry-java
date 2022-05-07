package retry;

import java.util.function.Predicate;

/**
 * The type Retry runnable.
 */
public final class RetryRunnable {
  private RetryRunnable() {
  }

  /**
   * Run.
   *
   * @param runnable the runnable
   * @param options  the options
   */
  public static void run(
      Runnable runnable,
      RetryOptions options) {
    run(runnable, null, options);
  }

  /**
   * Run.
   *
   * @param runnable  the runnable
   * @param predicate the predicate
   * @param options   the options
   */
  public static void run(
      Runnable runnable,
      Predicate<RuntimeException> predicate,
      RetryOptions options) {
    long delay = 0;
    int attempt = 0;
    for (; ; ) {
      try {
        runnable.run();
        return;
      } catch (RuntimeException ex) {
        boolean retry = predicate == null || predicate.test(ex);
        if (retry && ++attempt < options.getMax()) {
          delay = options.getBackoff().next(delay);
          try {
            Thread.sleep(delay);
          } catch (InterruptedException e) {
            throw new IllegalStateException(e);
          }

          continue;
        }

        throw ex;
      }
    }
  }
}

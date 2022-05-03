package retry;

import java.util.function.Predicate;

public final class RetryRunnable {
  private RetryRunnable() {
  }

  public static void run(
      Runnable runnable,
      RetryOptions options) {
    run(runnable, null, options);
  }

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

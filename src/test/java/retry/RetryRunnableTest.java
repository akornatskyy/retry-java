package retry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;

class RetryRunnableTest {
  private final static RetryOptions OPTIONS = RetryOptions.builder()
      .max(3)
      .backoff(new FixedBackoff(Duration.ofMillis(0)))
      .build();

  @Test
  void run() {
    Runnable runnable = Mockito.mock(Runnable.class);

    RetryRunnable.run(runnable, OPTIONS);

    Mockito.verify(runnable, Mockito.times(1)).run();
  }

  @Test
  void fails() {
    Runnable runnable = Mockito.mock(Runnable.class);
    Mockito.doThrow(RuntimeException.class).when(runnable).run();

    Assertions.assertThrows(
        RuntimeException.class,
        () -> RetryRunnable.run(runnable, OPTIONS));

    Mockito.verify(runnable, Mockito.times(3)).run();
  }

  @Test
  void predicateIgnoresError() {
    Runnable runnable = Mockito.mock(Runnable.class);
    Mockito.doThrow(RuntimeException.class).when(runnable).run();

    Assertions.assertThrows(
        RuntimeException.class,
        () -> RetryRunnable.run(
            runnable,
            (ex) -> ex instanceof IllegalArgumentException,
            OPTIONS));

    Mockito.verify(runnable, Mockito.times(1)).run();
  }

  @Test
  void predicateRetries() {
    Runnable runnable = Mockito.mock(Runnable.class);
    Mockito.doThrow(IllegalArgumentException.class).when(runnable).run();

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> RetryRunnable.run(
            runnable,
            (ex) -> ex instanceof IllegalArgumentException,
            OPTIONS));

    Mockito.verify(runnable, Mockito.times(3)).run();
  }
}
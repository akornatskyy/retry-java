package retry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RetryOptionsTest {

  @Test
  void builder() {
    Backoff backoff = Mockito.mock(Backoff.class);
    RetryOptions options = RetryOptions.builder()
        .backoff(backoff)
        .max(5)
        .build();

    Assertions.assertEquals(5, options.getMax());
    Assertions.assertEquals(backoff, options.getBackoff());
  }

  @Test
  void builderDefaults() {
    RetryOptions options = RetryOptions.builder()
        .backoff(Mockito.mock(Backoff.class))
        .build();

    Assertions.assertEquals(3, options.getMax());
  }
}
package retry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

class ExpBackoffTest {

  @Test
  void builder() {
    Assertions.assertNotNull(
        ExpBackoff.builder()
            .build());

    Assertions.assertNotNull(
        ExpBackoff.builder()
            .initial(Duration.ofSeconds(2))
            .multiplier(1.2)
            .factor(0.1)
            .build());
  }

  @Test
  void nextFixed() {
    Backoff backoff = ExpBackoff.builder()
        .initial(Duration.ofSeconds(1))
        .multiplier(1.0)
        .factor(0.0)
        .build();

    Assertions.assertEquals(1000, backoff.next(0));
    Assertions.assertEquals(1000, backoff.next(1000));
    Assertions.assertEquals(1000, backoff.next(1000));
  }

  @Test
  void nextWithMultiplier() {
    Backoff backoff = ExpBackoff.builder()
        .initial(Duration.ofSeconds(1))
        .multiplier(1.5)
        .factor(0.0)
        .build();

    Assertions.assertEquals(1000, backoff.next(0));
    Assertions.assertEquals(1500, backoff.next(1000));
    Assertions.assertEquals(2250, backoff.next(1500));
  }

  @RepeatedTest(5)
  void nextWithFactor() {
    Backoff backoff = ExpBackoff.builder()
        .initial(Duration.ofSeconds(1))
        .multiplier(1.0)
        .factor(0.2)
        .build();

    long next = backoff.next(0);

    Assertions.assertTrue(next >= 800);
    Assertions.assertTrue(next <= 1200);
  }

  @ParameterizedTest
  @ValueSource(doubles = {-1.0, 0.0, 0.9})
  void guardMultiplier(double multiplier) {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new ExpBackoff(Duration.ofSeconds(1), multiplier, 0.0));
  }

  @ParameterizedTest
  @ValueSource(doubles = {-2.0, -1.0, -0.01, 1.0, 2.0})
  void guardFactor(double factor) {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new ExpBackoff(Duration.ofSeconds(1), 1.0, factor));
  }
}
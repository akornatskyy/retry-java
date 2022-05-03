package retry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class JitterBackoffTest {

  @RepeatedTest(5)
  void next() {
    Backoff backoff = new JitterBackoff(Duration.ofSeconds(1), 0.2);

    long next = backoff.next(0);

    Assertions.assertTrue(next >= 800);
    Assertions.assertTrue(next <= 1200);
  }

  @Test
  void zeroFactor() {
    Backoff backoff = new JitterBackoff(Duration.ofSeconds(2), 0);

    long next = backoff.next(0);

    Assertions.assertEquals(2000, next);
  }

  @ParameterizedTest
  @ValueSource(doubles = {-2.0, -1.0, -0.01, 1.0, 2.0})
  void guardFactor(double factor) {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new JitterBackoff(Duration.ofSeconds(1), factor));
  }
}
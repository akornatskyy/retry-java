package retry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.time.Duration;

class FixedBackoffTest {

  @RepeatedTest(5)
  void next() {
    Backoff backoff = new FixedBackoff(Duration.ofSeconds(2));

    long next = backoff.next(0);

    Assertions.assertEquals(2000, next);
  }
}
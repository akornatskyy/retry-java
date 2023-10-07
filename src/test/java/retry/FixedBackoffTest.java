package retry;

import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

class FixedBackoffTest {

  @RepeatedTest(5)
  void next() {
    Backoff backoff = new FixedBackoff(Duration.ofSeconds(2));

    long next = backoff.next(0);

    Assertions.assertEquals(2000, next);
  }
}
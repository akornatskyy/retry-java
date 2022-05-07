package retry;

import java.time.Duration;
import java.util.Objects;

/**
 * The type Fixed backoff.
 */
public final class FixedBackoff implements Backoff {
  private final long delay;

  public FixedBackoff(Duration delay) {
    Objects.requireNonNull(delay, "delay");
    this.delay = delay.toMillis();
  }

  @Override
  public long next(long initial) {
    return this.delay;
  }
}

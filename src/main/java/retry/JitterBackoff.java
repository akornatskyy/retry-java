package retry;

import java.time.Duration;
import java.util.Objects;

/**
 * The type Jitter backoff.
 */
public final class JitterBackoff implements Backoff {
  private final double delay;
  private final double factor;

  /**
   * Instantiates a new Jitter backoff.
   *
   * @param delay  the base delay
   * @param factor the randomization factor
   */
  public JitterBackoff(Duration delay, double factor) {
    Objects.requireNonNull(delay, "delay");
    if (factor < 0.0 || factor >= 1.0) {
      throw new IllegalArgumentException("factor");
    }

    this.delay = delay.toMillis();
    this.factor = factor;
  }

  @Override
  public long next(long initial) {
    double delta = delay * factor;
    double min = delay - delta;
    double max = delay + delta;
    return (long) (min + Math.random() * (max - min + 1.0));
  }
}

package retry;

import java.time.Duration;
import java.util.Objects;

public final class ExpBackoff implements Backoff {
  private final double initial;
  private final double multiplier;
  private final double factor;

  public ExpBackoff(Duration initial, double multiplier, double factor) {
    Objects.requireNonNull(initial, "initial");
    if (multiplier < 1.0) {
      throw new IllegalArgumentException("multiplier");
    }

    if (factor < 0.0 || factor >= 1.0) {
      throw new IllegalArgumentException("factor");
    }

    this.initial = initial.toMillis();
    this.multiplier = multiplier;
    this.factor = factor;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public long next(long last) {
    double current = last == 0 ? initial : last * multiplier;
    double delta = current * factor;
    double min = current - delta;
    double max = current + delta;
    return (long)(min + Math.random() * (max - min + 1.0));
  }

  static class Builder {
    private Duration initial = Duration.ofMillis(500);
    private double multiplier = 1.5;
    private double factor = 0.2;

    public Builder initial(Duration initial) {
      this.initial = initial;
      return this;
    }

    public Builder multiplier(double multiplier) {
      this.multiplier = multiplier;
      return this;
    }

    public Builder factor(double factor) {
      this.factor = factor;
      return this;
    }

    public ExpBackoff build() {
      return new ExpBackoff(initial, multiplier, factor);
    }
  }
}

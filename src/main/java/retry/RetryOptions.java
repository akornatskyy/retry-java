package retry;

import java.util.Objects;

/**
 * The type Retry options.
 */
public final class RetryOptions {
  private final int max;
  private final Backoff backoff;

  private RetryOptions(int max, Backoff backoff) {
    Objects.requireNonNull(backoff, "backoff");
    this.max = max;
    this.backoff = backoff;
  }

  public int getMax() {
    return max;
  }

  public Backoff getBackoff() {
    return backoff;
  }

  public static RetryOptions.Builder builder() {
    return new Builder();
  }

  static class Builder {
    private int max = 3;
    private Backoff backoff;

    public Builder max(int max) {
      this.max = max;
      return this;
    }

    public Builder backoff(Backoff backoff) {
      this.backoff = backoff;
      return this;
    }

    public RetryOptions build() {
      return new RetryOptions(max, backoff);
    }
  }
}

package retry;

/**
 * The interface Backoff.
 */
public interface Backoff {
  long next(long initial);
}

package retry;

public interface Backoff {
  long next(long initial);
}

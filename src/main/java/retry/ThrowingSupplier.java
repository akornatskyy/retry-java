package retry;

/**
 * The functional interface that can be used for a code that returns
 * an object and potentially throws a {@link Exception}.
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {

  /**
   * Get a result, potentially throwing an exception.
   *
   * @return a result
   */
  T get() throws Exception;
}

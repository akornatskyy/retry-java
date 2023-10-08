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
   * @return A result.
   * @throws Exception This method may throw exceptions.
   */
  T get() throws Exception;
}

package retry;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RetryThrowingSupplierTest {
  private final static RetryOptions OPTIONS = RetryOptions.builder()
      .max(3)
      .backoff(new FixedBackoff(Duration.ofMillis(0)))
      .build();

  @Test
  void get() throws Exception {
    ThrowingSupplier<Integer> supplier = Mockito.mock(ThrowingSupplier.class);
    Mockito.when(supplier.get()).thenReturn(100);

    int result = RetryThrowingSupplier.get(supplier, OPTIONS);

    Assertions.assertEquals(100, result);
    Mockito.verify(supplier, Mockito.times(1)).get();
  }

  @Test
  void fails() throws Exception {
    ThrowingSupplier<Boolean> supplier = Mockito.mock(ThrowingSupplier.class);
    Mockito.when(supplier.get()).thenThrow(RuntimeException.class);

    Assertions.assertThrows(
        RuntimeException.class,
        () -> RetryThrowingSupplier.get(supplier, OPTIONS));

    Mockito.verify(supplier, Mockito.times(3)).get();
  }

  @Test
  void predicateIgnoresError() throws Exception {
    ThrowingSupplier<Boolean> supplier = Mockito.mock(ThrowingSupplier.class);
    Mockito.when(supplier.get()).thenThrow(Exception.class);

    Assertions.assertThrows(
        Exception.class,
        () -> RetryThrowingSupplier.get(
            supplier,
            (result, ex) -> ex instanceof IllegalArgumentException,
            OPTIONS));

    Mockito.verify(supplier, Mockito.times(1)).get();
  }

  @Test
  void predicateRetriesError() throws Exception {
    ThrowingSupplier<Boolean> supplier = Mockito.mock(ThrowingSupplier.class);
    Mockito.when(supplier.get()).thenThrow(IOException.class);

    Assertions.assertThrows(
        IOException.class,
        () -> RetryThrowingSupplier.get(
            supplier,
            (result, ex) -> ex instanceof IOException,
            OPTIONS));

    Mockito.verify(supplier, Mockito.times(3)).get();
  }

  @Test
  void predicateRetriesResult() throws Exception {
    ThrowingSupplier<Integer> supplier = Mockito.mock(ThrowingSupplier.class);
    Mockito.when(supplier.get()).thenReturn(100);

    int r = RetryThrowingSupplier.get(
        supplier,
        (result, ex) -> result != 200,
        OPTIONS);

    Assertions.assertEquals(100, r);
    Mockito.verify(supplier, Mockito.times(3)).get();
  }
}
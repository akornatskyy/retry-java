package retry;

import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RetrySupplierTest {
  private final static RetryOptions OPTIONS = RetryOptions.builder()
      .max(3)
      .backoff(new FixedBackoff(Duration.ofMillis(0)))
      .build();

  @Test
  void get() {
    Supplier<Integer> supplier = Mockito.mock(Supplier.class);
    Mockito.when(supplier.get()).thenReturn(100);

    int result = RetrySupplier.get(supplier, OPTIONS);

    Assertions.assertEquals(100, result);
    Mockito.verify(supplier, Mockito.times(1)).get();
  }

  @Test
  void fails() {
    Supplier<Boolean> supplier = Mockito.mock(Supplier.class);
    Mockito.when(supplier.get()).thenThrow(RuntimeException.class);

    Assertions.assertThrows(
        RuntimeException.class,
        () -> RetrySupplier.get(supplier, OPTIONS));

    Mockito.verify(supplier, Mockito.times(3)).get();
  }

  @Test
  void predicateIgnoresError() {
    Supplier<Boolean> supplier = Mockito.mock(Supplier.class);
    Mockito.when(supplier.get()).thenThrow(RuntimeException.class);

    Assertions.assertThrows(
        RuntimeException.class,
        () -> RetrySupplier.get(
            supplier,
            (result, ex) -> ex instanceof IllegalArgumentException,
            OPTIONS));

    Mockito.verify(supplier, Mockito.times(1)).get();
  }

  @Test
  void predicateRetriesError() {
    Supplier<Boolean> supplier = Mockito.mock(Supplier.class);
    Mockito.when(supplier.get()).thenThrow(IllegalArgumentException.class);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> RetrySupplier.get(
            supplier,
            (result, ex) -> ex instanceof IllegalArgumentException,
            OPTIONS));

    Mockito.verify(supplier, Mockito.times(3)).get();
  }

  @Test
  void predicateRetriesResult() {
    Supplier<Integer> supplier = Mockito.mock(Supplier.class);
    Mockito.when(supplier.get()).thenReturn(100);

    int r = RetrySupplier.get(
        supplier,
        (result, ex) -> result != 200,
        OPTIONS);

    Assertions.assertEquals(100, r);
    Mockito.verify(supplier, Mockito.times(3)).get();
  }
}
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class RetryServiceTest {

    @Test
    public void testRetryService() {
        RemoteService service = mock(RemoteService.class);
        int maxAttempts = 2;
        RetryConfig config = RetryConfig
                .custom()
                .maxAttempts(maxAttempts)
                // .retryExceptions(RuntimeException.class)
                .waitDuration(java.time.Duration.ofMillis(100))
                .build();
        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("myRetry");
        retry.getEventPublisher().onRetry(event -> System.out.println("Retried an event: , " + event));
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        when(service.process(anyInt())).thenThrow(new RuntimeException());
        try {
            decorated.apply(1);
            fail("Expected an exception to be thrown if all retries failed");
        } catch (Exception e) {
            verify(service, times(maxAttempts)).process(anyInt());
        }
    }

}

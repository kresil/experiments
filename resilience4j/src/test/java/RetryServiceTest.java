import exceptions.NetworkException;
import exceptions.RemoteServiceException;
import exceptions.WebServiceException;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static io.github.resilience4j.core.CallableUtils.recover;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class RetryServiceTest {

    @Test
    public void decoratesAFunctionalInterfaceWithCustomConfig() {
        // given: a remote service
        RemoteService service = mock(RemoteService.class);

        // and: a retry configuration
        int maxAttempts = 10;
        RetryConfig config = RetryConfig
                .custom()
                .maxAttempts(maxAttempts)
                // .retryExceptions(RemoteServiceException.class)
                .retryExceptions(RemoteServiceException.class)
                .waitDuration(Duration.ofMillis(100))
                .build();

        // when: the retry is registered
        RetryRegistry registry = RetryRegistry.of(config);
        Retry retry = registry.retry("myRetry");

        // and: the service is decorated with the retry mechanism
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // given: a remote service configuration to always throw an exception to simulate a failure
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"));
        try {

            // when: the service is called
            decorated.apply(1);
            fail("Expected an exception to be thrown if all retries failed");
        } catch (Exception e) {
            // then: it should be retried the maximum number of times specified in the retry configuration
            verify(service, times(maxAttempts)).process(anyInt());
        }
    }

    @Test
    public void decoratesASupplierWithDefaultConfig() throws Exception {
        // given: a remote service
        RemoteService remoteService = mock(RemoteService.class);

        // and: a retry with default configuration
        Retry retry = Retry.ofDefaults("id");

        // when: the service is decorated with the retry mechanism using a supplier
        CheckedSupplier<String> retryableSupplier = Retry
                .decorateCheckedSupplier(retry, remoteService::message);

        // and: a callback to recover from the exception
        Callable<String> callable = () -> {
            try {
                return retryableSupplier.get();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
        String recoveryMessage = "Hello world from recovery function";
        Callable<String> recoveredCallable = recover(callable, (Throwable t) -> recoveryMessage);
        Try<String> result = Try.call(recoveredCallable);

        // when: the service is called and throws an exception
        when(remoteService.message())
                .thenThrow(new NetworkException("Thanks Vodafone!"));

        // then: the service should be retried the default number of times
        then(remoteService)
                .should(times(3))
                .message();

        // and the exception should be handled by the recovery function
        assertEquals(result.get(), recoveryMessage);
    }
}

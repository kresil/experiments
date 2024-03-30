import exceptions.NetworkException;
import exceptions.RemoteServiceException;
import exceptions.WebServiceException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Logger;

import static io.github.resilience4j.core.CallableUtils.recover;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class RetryServiceTest {

    static Logger logger = Logger.getLogger(RetryServiceTest.class.getName());

    @Test
    public void decoratesAFunctionalInterfaceWithCustomConfig() {
        // given: a remote service
        RemoteService service = mock(RemoteService.class);

        // and: a retry configuration
        int maxAttempts = 10;
        RetryConfig config = RetryConfig
                .custom()
                .maxAttempts(maxAttempts)
                .retryExceptions(RemoteServiceException.class) // to check subtype support
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

        // and: the exception should be handled by the recovery function
        assertEquals(result.get(), recoveryMessage);
    }

    @Test
    public void configWithExponentionalBackoffIntervalFunction() {
        // given: an interval function with exponential backoff
        IntervalFunction intervalWithExponentialBackoff =
                IntervalFunction.ofExponentialBackoff(100, 2);

        // and: a retry configuration
        RetryConfig config = RetryConfig
                .custom()
                .intervalFunction(intervalWithExponentialBackoff)
                .build();

        // when: the retry is registered
        Retry retry = Retry.of("name", config);

        // and: each event is configured with a logger
        logAllRetryEvents(retry);

        // and: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // when: a remote service configuration to always throw an exception to simulate a failure
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"));

        // and: the service is called
        try {
            decorated.apply(1);
            fail("Expected an exception to be thrown if all retries failed");
        } catch (Exception e) {
            // then: it should be retried the maximum number of times specified in the retry configuration
            verify(service, times(3)).process(anyInt());
        }
    }

    @Test
    public void configWithCustomIntervalFunction() {
        // given: a custom interval function
        IntervalFunction customIntervalFunction =
                IntervalFunction.of(1000, nrOfAttempts -> nrOfAttempts + 1000);

        // and: a retry configuration
        int maxAttempts = 5;
        RetryConfig config = RetryConfig
                .custom()
                .maxAttempts(maxAttempts)
                .intervalFunction(customIntervalFunction)
                .build();

        // when: the retry is registered
        Retry retry = Retry.of("name", config);

        // and: each event is configured with a logger
        logAllRetryEvents(retry);

        // and: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // given: a remote service configuration to always throw an exception to simulate a failure
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"));

        // when: the service is called
        try {
            decorated.apply(1);
            fail("Expected an exception to be thrown if all retries failed");
        } catch (Exception e) {
            // then: it should be retried the maximum number of times specified in the retry configuration
            verify(service, times(maxAttempts)).process(anyInt());
        }
    }

    @Test
    public void logAllRetryStatusInARegistry() {
        // given: a retry registry
        RetryRegistry registry = RetryRegistry.ofDefaults();

        // and: a logger is registered to log all entries state changes
        registry.getEventPublisher()
                .onEntryAdded(entryAddedEvent -> {
                    Retry addedRetry = entryAddedEvent.getAddedEntry();
                    logger.info("Retry " + addedRetry.getName() + " added");
                })
                .onEntryRemoved(entryRemovedEvent -> {
                    Retry removedRetry = entryRemovedEvent.getRemovedEntry();
                    logger.info("Retry " + removedRetry.getName() + " removed");
                })
                .onEntryReplaced(entryReplacedEvent -> {
                    Retry newRetry = entryReplacedEvent.getNewEntry();
                    Retry oldRetry = entryReplacedEvent.getOldEntry();
                    logger.info("Retry " + oldRetry.getName() + " replaced with " + newRetry.getName());
                });

        // and: a set of retry configurations
        RetryConfig defaultConfig = RetryConfig.ofDefaults();
        RetryConfig customConfig = RetryConfig.custom().build();

        // when: the retries are registered, removed or replaced
        String defaultConfigName = "defaultConfig";
        String customConfigName = "customConfig";
        Retry defaultRetry = registry.retry(defaultConfigName, defaultConfig);
        Retry customRetry = registry.retry(customConfigName, customConfig);
        registry.getAllRetries().forEach(retry -> logger.info("RetryName: " + retry.getName()));
        registry.remove(defaultConfigName);
        registry.replace(customConfigName, defaultRetry);

        // then: the registry event publisher should log all changes

    }

    private static void logAllRetryEvents(Retry retry) {
        retry.getEventPublisher()
                .onRetry(event ->
                        logger.info("Interval: " + event.getWaitInterval()
                                + " - Event: " + event.getEventType()))
                .onError(event -> logger.info("Error: " + event.getEventType()))
                .onIgnoredError(event -> logger.info("Ignored error: " + event.getEventType()))
                .onSuccess(event -> logger.info("Success: " + event.getEventType()));

        // or retry.getEventPublisher().onEvent(event -> logger.info(event.toString())); to log all events indiscriminately
    }
}

package retry;

import exceptions.NetworkException;
import exceptions.RemoteServiceException;
import exceptions.WebServiceException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.core.SupplierUtils;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;
import service.RemoteService;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static io.github.resilience4j.core.CallableUtils.recover;
import static io.github.resilience4j.core.SupplierUtils.recover;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class RetryServiceTest {

    static Logger logger = Logger.getLogger(RetryServiceTest.class.getName());

    @Test
    public void decoratesAFunctionWithCustomConfig() {
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
    public void decoratesASupplierWithFailureRecovery() {
        // given: a retry configuration
        RetryConfig config = RetryConfig.ofDefaults();

        // and: a retry instance
        Retry retry = Retry.of("myRetry", config);

        // and: some service that always fails is configured to recover from failure
        Supplier<String> supplier = () -> {
            throw new NetworkException("Thanks Vodafone!");
        };
        String recoveryMessage = "Hello world from recovery function";
        Supplier<String> supplierWithRecovery = recover(supplier, (exception) -> recoveryMessage);

        // when: the supplier is decorated with the retry mechanism
        String result = retry.executeSupplier(supplierWithRecovery);

        // then: the supplier should recover from the exception
        assertEquals(result, recoveryMessage);
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

        // when: the retry is created
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

        // when: the retry is created
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

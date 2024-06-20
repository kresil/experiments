package retry;

import exceptions.BusinessServiceException;
import exceptions.NetworkException;
import exceptions.RemoteServiceException;
import exceptions.WebServiceException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import service.RemoteService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static io.github.resilience4j.core.SupplierUtils.recover;
import static io.github.resilience4j.retry.event.RetryEvent.Type;
import static org.junit.jupiter.api.Assertions.*;
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
            decorated.apply(anyInt());
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

        // when: a service is decorated with the retry mechanism
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
            decorated.apply(anyInt());
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

        // when: a service is decorated with the retry mechanism
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
            decorated.apply(anyInt());
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
        @SuppressWarnings("unused")
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

    @Test
    public void retryStateMachineAndContextPreservation() {
        // given: all types of possible retry events
        System.out.println("RetryEvent.Type: " + Arrays.toString(Type.values()));

        // and: a retry configuration
        int maxAttempts = 3;
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .ignoreExceptions(NetworkException.class)
                .build();

        // and: a retry instance
        Retry retry = Retry.of("somename", config);

        // and: a retry list to store the retry event
        List<Type> retryEvents = new ArrayList<>();

        // and: a set of listeners are registered to log all retry events
        retry.getEventPublisher()
                .onRetry(event -> {
                    Type type = event.getEventType();
                    logger.info("Interval: " + event.getWaitInterval()
                            + " - Event: " + type);
                    retryEvents.add(type);
                })
                .onError(event -> {
                    Type type = event.getEventType();
                    logger.info("Error: " + type);
                    retryEvents.add(type);
                })
                .onIgnoredError(event -> {
                    Type type = event.getEventType();
                    logger.info("Ignored error: " + type);
                    retryEvents.add(type);
                })
                .onSuccess(event -> {
                    Type type = event.getEventType();
                    logger.info("Success: " + type);
                    retryEvents.add(type);
                });

        // when: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // and: a remote service configuration to always throw an exception to simulate a failure
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"));

        // when: the service is called
        try {
            decorated.apply(anyInt());
            fail("Expected an exception to be thrown if all retries failed");
        } catch (Exception e) {
            // then: it should be retried the maximum number of times specified in the retry configuration
            assertEquals(maxAttempts, retryEvents.size());
            List<Type> expectedList = List.of(
                    Type.RETRY,
                    Type.RETRY,
                    Type.ERROR
            );
            assertTrue(expectedList.containsAll(retryEvents));
        }

        // when: a remote service configuration that does not always throw an exception
        reset(service);
        retryEvents.clear();
        when(service.process(anyInt())).then(new Answer<Integer>() {
            private int count = 0;

            public Integer answer(InvocationOnMock invocation) {
                if (count++ < 2) {
                    throw new WebServiceException("BAM!");
                } else {
                    return 1;
                }
            }
        });

        // when: the service is called
        try {
            decorated.apply(anyInt());
        } catch (Exception e) {
            // then: it should be retried the maximum number of times specified in the retry configuration
            assertEquals(maxAttempts, retryEvents.size());
            // and: it should not share the same retry context
            List<Type> expectedList = List.of(
                    Type.RETRY,
                    Type.RETRY,
                    Type.SUCCESS
            );
            assertTrue(expectedList.containsAll(retryEvents));
        }

        // when: a remote service configuration that throws an ignored exception
        reset(service);
        retryEvents.clear();
        when(service.process(anyInt())).then(new Answer<Integer>() {
            private int count = 0;

            public Integer answer(InvocationOnMock invocation) {
                if (count++ < 2) {
                    throw new WebServiceException("BAM!");
                } else {
                    throw new NetworkException("Thanks Vodafone!");
                }
            }
        });

        // when: the service is called
        try {
            decorated.apply(anyInt());
        } catch (Exception e) {
            // then: it should be retried the maximum number of times specified in the retry configuration
            assertEquals(maxAttempts, retryEvents.size());
            // and: it should not share the same retry context
            List<Type> expectedList = List.of(
                    Type.RETRY,
                    Type.RETRY,
                    Type.IGNORED_ERROR // which means the retry was successful
            );
            assertTrue(expectedList.containsAll(retryEvents));
        }

    }

    @Test
    public void ignoredErrorStateIsADifferentTypeOfSucess() {
        // given: a retry configuration
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .ignoreExceptions(NetworkException.class)
                .build();

        // and: a retry instance
        Retry retry = Retry.of("somename", config);

        // and: a retry list to store the retry event
        List<Type> retryEvents = new ArrayList<>();

        // and: a set of listeners are registered to log all retry events
        retry.getEventPublisher()
                .onRetry(event -> {
                    Type type = event.getEventType();
                    logger.info("Interval: " + event.getWaitInterval()
                            + " - Event: " + type);
                    retryEvents.add(type);
                })
                .onError(event -> {
                    Type type = event.getEventType();
                    logger.info("Error: " + type);
                    retryEvents.add(type);
                })
                .onIgnoredError(event -> {
                    Type type = event.getEventType();
                    logger.info("Ignored error: " + type);
                    retryEvents.add(type);
                })
                .onSuccess(event -> {
                    Type type = event.getEventType();
                    logger.info("Success: " + type);
                    retryEvents.add(type);
                });

        // when: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // and: a remote service configuration that throws an ignored exception
        when(service.process(anyInt()))
                .thenThrow(new NetworkException("Thanks Vodafone!"));

        // when: the service is called
        try {
            decorated.apply(anyInt());
            fail("Expected an exception to be thrown if this exception is ignored");
        } catch (Exception e) {
            // then: it should not be retried once
            verify(service, times(1)).process(anyInt());

            // and: it should register the ignored error
            assertEquals(1, retryEvents.size());
            assertEquals(Type.IGNORED_ERROR, retryEvents.get(0));
        }
    }

    @Test
    public void retrieveRetryInstancesFromRegistry() {
        // given: a retry registry
        RetryRegistry registry = RetryRegistry.ofDefaults();

        // and: a default retry configuration
        RetryConfig defaultConfig = RetryConfig.ofDefaults();

        // when: the retry is registered with a name
        String retryName = "myRetry";
        Retry retry = registry.retry(retryName, defaultConfig);

        // and: the retry is retrieved from the registry using the same name and configuration
        Retry retryWithSameNameAndConfig = registry.retry(retryName, defaultConfig);

        // then: the retry instances should be strictly equal
        assertSame(retry, retryWithSameNameAndConfig);

        // when: the retry is retrieved from the registry using the same name and a different configuration
        RetryConfig newConfig = RetryConfig.custom().maxAttempts(5).build();
        Retry retryWithSameNameAndDiffConfig = registry.retry(retryName, newConfig);

        // then: the retry instances should be strictly equal
        assertSame(retry, retryWithSameNameAndDiffConfig);
        assertEquals(retry.getRetryConfig(), retryWithSameNameAndDiffConfig.getRetryConfig());

        // when: the retry is retrieved from the registry using a different name independent of the configuration
        String newRetryName = "newRetry";
        Retry newRetry = registry.retry(newRetryName, defaultConfig);

        // then: the retry instances should not be strictly equal
        assertNotSame(retry, newRetry);
    }

    @Test
    public void retryCreationWithAndWithoutRegistry() {
        // given: a retry registry
        RetryRegistry registry = RetryRegistry.ofDefaults();

        // and: a default retry configuration
        RetryConfig defaultConfig = RetryConfig.ofDefaults();

        // when: the retry is created and registered with a name
        String retryName = "default";
        Retry retry = registry.retry(retryName, defaultConfig);

        // and: another retry is created without registration
        Retry retryWithoutRegistry = Retry.of(retryName, defaultConfig);

        // then: the retry instances should not be strictly equal
        assertNotSame(retry, retryWithoutRegistry);
        // and: not equal altogether
        assertNotEquals(retry, retryWithoutRegistry);
    }

    @Test
    public void retryInheritsBaseConfigFromRegistry() {
        // given: a custom retry configuration
        RetryConfig baseConfig = RetryConfig.custom()
                .maxAttempts(3)
                .failAfterMaxAttempts(true)
                .build();

        // and: a retry registry with a base configuration
        RetryRegistry registry = RetryRegistry.of(baseConfig);

        // when: the retry is created using the registry
        String retryName = "myRetry";
        Retry retry = registry.retry(retryName);

        // then: the retry should inherit the base configuration
        assertSame(baseConfig, retry.getRetryConfig());

        // when: the retry is created with a custom configuration from the registry
        RetryConfig customConfig = RetryConfig.custom()
                .maxAttempts(5)
                .failAfterMaxAttempts(false)
                .build();
        Retry customRetry = registry.retry("customRetry", customConfig);

        // then: the retry should use the custom configuration instead of the base configuration
        assertNotEquals(baseConfig.getMaxAttempts(), customRetry.getRetryConfig().getMaxAttempts());
        assertFalse(customRetry.getRetryConfig().isFailAfterMaxAttempts()); // base configuration had this set to true
    }

    @Test
    public void resetARetryContextUponSuccess() {
        // given: a retry configuration
        int maxAttempts = 5;
        RetryConfig config = RetryConfig.custom()
                .ignoreExceptions(NetworkException.class)
                .maxAttempts(maxAttempts)
                .build();

        // and: a retry instance
        Retry retry = Retry.of("somename", config);

        // and: a retry list to store the retry event
        List<Type> retryEvents = new ArrayList<>();

        // and: a set of listeners are registered to log all retry events
        retry.getEventPublisher()
                .onRetry(event -> {
                    Type type = event.getEventType();
                    logger.info("Interval: " + event.getWaitInterval()
                            + " - Event: " + type);
                    retryEvents.add(type);
                })
                .onError(event -> {
                    Type type = event.getEventType();
                    logger.info("Error: " + type);
                    retryEvents.add(type);
                })
                .onIgnoredError(event -> {
                    Type type = event.getEventType();
                    logger.info("Ignored error: " + type);
                    retryEvents.add(type);
                })
                .onSuccess(event -> {
                    Type type = event.getEventType();
                    logger.info("Success: " + type);
                    retryEvents.add(type);
                });

        // when: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // and: a remote service configuration that throws an ignored exception after the second retry
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"))
                .thenThrow(new WebServiceException("BAM!"))
                .thenThrow(new NetworkException("Thanks Vodafone!"));

        // when: the service is called
        try {
            decorated.apply(anyInt());
            fail("Expected the retry to fail after second attempt");
        } catch (Exception e) {
            // then: it should be retried two times and then ignored
            verify(service, times(3)).process(anyInt());
            assertEquals(3, retryEvents.size());
            List<Type> expectedList = List.of(
                    Type.RETRY,
                    Type.RETRY,
                    Type.IGNORED_ERROR
            );
            assertTrue(expectedList.containsAll(retryEvents));
        }

        // and: a remote service configuration that always throws an exception
        reset(service);
        retryEvents.clear();
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"));

        // when: the service is called
        try {
            decorated.apply(anyInt());
        } catch (Exception e) {
            // then: it should be retried the maximum number of times specified in the retry configuration
            verify(service, times(maxAttempts)).process(anyInt());
            assertEquals(maxAttempts, retryEvents.size());
            // and: it should not share the same retry context
            List<Type> expectedList = new ArrayList<>();
            for (int i = 0; i < maxAttempts - 1; i++) {
                expectedList.add(Type.RETRY);
            }
            expectedList.add(Type.ERROR);
            assertTrue(expectedList.containsAll(retryEvents));
        }
    }

    @Test
    public void noContextPreservationOnDecoratorCall() {
        // given: a retry configuration
        int maxAttempts = 5;
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .build();

        // and: a retry instance
        Retry retry = Retry.of("somename", config);

        // and: a retry list to store the retry event
        List<Type> retryEvents = new ArrayList<>();

        // and: a set of listeners are registered to log all retry events
        retry.getEventPublisher()
                .onRetry(event -> {
                    Type type = event.getEventType();
                    logger.info("Interval: " + event.getWaitInterval()
                            + " - Event: " + type);
                    retryEvents.add(type);
                })
                .onError(event -> {
                    Type type = event.getEventType();
                    logger.info("Error: " + type);
                    retryEvents.add(type);
                })
                .onIgnoredError(event -> {
                    Type type = event.getEventType();
                    logger.info("Ignored error: " + type);
                    retryEvents.add(type);
                })
                .onSuccess(event -> {
                    Type type = event.getEventType();
                    logger.info("Success: " + type);
                    retryEvents.add(type);
                });

        // when: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // and: a remote service configuration that always throws an exception
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"));

        // when: the service is called
        try {
            decorated.apply(anyInt());
            fail("Expected the retry to fail after second attempt");
        } catch (Exception e) {
            // then: it should be retried maxAttempts times
            verify(service, times(maxAttempts)).process(anyInt());

            // and: the retry context should not be preserved
            assertEquals(maxAttempts, retryEvents.size());
            // 4 * RETRY + 1 * ERROR
            List<Type> expectedList = new ArrayList<>();
            for (int i = 0; i < maxAttempts - 1; i++) {
                expectedList.add(Type.RETRY);
            }
            expectedList.add(Type.ERROR);
            assertTrue(expectedList.containsAll(retryEvents));
        }

        // when: the service is called again
        try {
            decorated.apply(anyInt());
            fail("Expected the retry to fail after second attempt");
        } catch (Exception e) {
            // then: it should be retried maxAttempts times again
            // note: not reset mock here, so the call count should be maxAttempts * 2
            verify(service, times(maxAttempts * 2)).process(anyInt());
        }
    }

    @Test
    public void ifRetryIsNotTriggeredThenNoEventIsPublished() {
        // given: a retry configuration
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .ignoreExceptions(NetworkException.class)
                .build();

        // and: a retry instance
        Retry retry = Retry.of("somename", config);

        // and: a retry list to store the retry event
        List<Type> retryEvents = new ArrayList<>();

        // and: a set of listeners are registered to log all retry events
        retry.getEventPublisher()
                .onRetry(event -> {
                    Type type = event.getEventType();
                    logger.info("Interval: " + event.getWaitInterval()
                            + " - Event: " + type);
                    retryEvents.add(type);
                })
                .onError(event -> {
                    Type type = event.getEventType();
                    logger.info("Error: " + type);
                    retryEvents.add(type);
                })
                .onIgnoredError(event -> {
                    Type type = event.getEventType();
                    logger.info("Ignored error: " + type);
                    retryEvents.add(type);
                })
                .onSuccess(event -> {
                    Type type = event.getEventType();
                    logger.info("Success: " + type);
                    retryEvents.add(type);
                });

        // and: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // and: a remote service configuration that always returns a success
        when(service.process(anyInt())).thenReturn(1);

        // when: the service is called
        decorated.apply(anyInt());

        // then: no retry event should be published
        assertTrue(retryEvents.isEmpty());
    }

    @Test
    public void ignoredErrorOnFirstCall() {
        // given: a retry configuration
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .ignoreExceptions(NetworkException.class)
                .build();

        // and: a retry instance
        Retry retry = Retry.of("somename", config);

        // and: a retry list to store the retry event
        List<Type> retryEvents = new ArrayList<>();

        // and: a set of listeners are registered to log all retry events
        retry.getEventPublisher()
                .onRetry(event -> {
                    Type type = event.getEventType();
                    logger.info("Interval: " + event.getWaitInterval()
                            + " - Event: " + type);
                    retryEvents.add(type);
                })
                .onError(event -> {
                    Type type = event.getEventType();
                    logger.info("Error: " + type);
                    retryEvents.add(type);
                })
                .onIgnoredError(event -> {
                    Type type = event.getEventType();
                    logger.info("Ignored error: " + type);
                    retryEvents.add(type);
                })
                .onSuccess(event -> {
                    Type type = event.getEventType();
                    logger.info("Success: " + type);
                    retryEvents.add(type);
                });

        // when: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // and: a remote service configuration that always throws an ignored exception
        when(service.process(anyInt()))
                .thenThrow(new NetworkException("Thanks Vodafone!"));

        // when: the service is called
        try {
            decorated.apply(anyInt());
            fail("Expected the retry to fail after first attempt");
        } catch (Exception e) {
            // then: it should not be retried
            verify(service, times(1)).process(anyInt());
            assertEquals(1, retryEvents.size());
            assertEquals(Type.IGNORED_ERROR, retryEvents.get(0));
        }
    }

    @Test
    public void ignoredErrorOnSecondCall() {
        // given: a retry configuration
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .ignoreExceptions(NetworkException.class)
                .build();

        // and: a retry instance
        Retry retry = Retry.of("somename", config);

        // and: a retry list to store the retry event
        List<Type> retryEvents = new ArrayList<>();

        // and: a set of listeners are registered to log all retry events
        retry.getEventPublisher()
                .onRetry(event -> {
                    Type type = event.getEventType();
                    logger.info("Interval: " + event.getWaitInterval()
                            + " - Event: " + type);
                    retryEvents.add(type);
                })
                .onError(event -> {
                    Type type = event.getEventType();
                    logger.info("Error: " + type);
                    retryEvents.add(type);
                })
                .onIgnoredError(event -> {
                    Type type = event.getEventType();
                    logger.info("Ignored error: " + type);
                    retryEvents.add(type);
                })
                .onSuccess(event -> {
                    Type type = event.getEventType();
                    logger.info("Success: " + type);
                    retryEvents.add(type);
                });

        // when: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // and: a remote service configuration that throws an ignored exception after the first call
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"))
                .thenThrow(new NetworkException("Thanks Vodafone!"));

        // when: the service is called
        try {
            decorated.apply(anyInt());
            fail("Expected the retry to fail after second attempt");
        } catch (Exception e) {
            // then: it should be retried once
            verify(service, times(2)).process(anyInt());
            assertEquals(2, retryEvents.size());
            List<Type> expectedList = List.of(
                    Type.RETRY,
                    Type.IGNORED_ERROR
            );
            assertTrue(expectedList.containsAll(retryEvents));
        }
    }

    @Test
    public void errorOnFirstCallWithMinimumAttempts() {
        // given: a retry configuration
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(1)
                .build();

        // and: a retry instance
        Retry retry = Retry.of("somename", config);

        // and: a retry list to store the retry event
        List<Type> retryEvents = new ArrayList<>();

        // and: a set of listeners are registered to log all retry events
        retry.getEventPublisher()
                .onRetry(event -> {
                    Type type = event.getEventType();
                    logger.info("Interval: " + event.getWaitInterval()
                            + " - Event: " + type);
                    retryEvents.add(type);
                })
                .onError(event -> {
                    Type type = event.getEventType();
                    logger.info("Error: " + type);
                    retryEvents.add(type);
                })
                .onIgnoredError(event -> {
                    Type type = event.getEventType();
                    logger.info("Ignored error: " + type);
                    retryEvents.add(type);
                })
                .onSuccess(event -> {
                    Type type = event.getEventType();
                    logger.info("Success: " + type);
                    retryEvents.add(type);
                });

        // when: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // and: a remote service configuration that always throws an exception
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"));

        // when: the service is called
        try {
            decorated.apply(anyInt());
            fail("Expected the retry to fail after first attempt");
        } catch (Exception e) {
            // then: it should not be retried once
            verify(service, times(1)).process(anyInt());
            assertEquals(1, retryEvents.size());
            assertEquals(Type.ERROR, retryEvents.get(0));
        }
    }

    @Test
    public void overrideABaseConfigPolicy() {
        // given: a base retry configuration
        int baseConfigAttempts = 2;
        RetryConfig baseConfig = RetryConfig.custom()
                .maxAttempts(baseConfigAttempts)
                .failAfterMaxAttempts(true)
                .build();

        // and: a custom retry configuration based on the base configuration that overrides at least one policy
        int customConfigAttempts = 5;
        RetryConfig customConfig = RetryConfig.from(baseConfig)
                .maxAttempts(customConfigAttempts)
                .build();

        // then: the custom configuration should override the base configuration for each policy that is different
        assertNotEquals(baseConfig.getMaxAttempts(), customConfig.getMaxAttempts());

        // and: the custom configuration should inherit the base configuration for each policy that is the same
        assertEquals(baseConfig.isFailAfterMaxAttempts(), customConfig.isFailAfterMaxAttempts());

    }

    @Test
    public void ignoreExceptionsOnRetry() {
        // given: a retry configuration
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .retryExceptions(NetworkException.class)
                .ignoreExceptions(BusinessServiceException.class)
                .build();

        // and: a retry instance
        Retry retry = Retry.of("somename", config);

        // and: a retry list to store the retry event
        List<Type> retryEvents = new ArrayList<>();

        // and: a set of listeners are registered to log all retry events
        retry.getEventPublisher()
                .onRetry(event -> {
                    Type type = event.getEventType();
                    logger.info("Interval: " + event.getWaitInterval()
                            + " - Event: " + type);
                    retryEvents.add(type);
                })
                .onError(event -> {
                    Type type = event.getEventType();
                    logger.info("Error: " + type);
                    retryEvents.add(type);
                })
                .onIgnoredError(event -> {
                    Type type = event.getEventType();
                    logger.info("Ignored error: " + type);
                    retryEvents.add(type);
                })
                .onSuccess(event -> {
                    Type type = event.getEventType();
                    logger.info("Success: " + type);
                    retryEvents.add(type);
                });

        // when: a service is decorated with the retry mechanism
        RemoteService service = mock(RemoteService.class);
        Function<Integer, Void> decorated
                = Retry.decorateFunction(retry, (Integer s) -> {
            service.process(s);
            return null;
        });

        // and: a remote service configuration that can throw an ignored exception
        when(service.process(anyInt()))
                .thenThrow(
                        new NetworkException("Thanks Vodafone!"),
                        new BusinessServiceException("BAM!"),
                        new WebServiceException("BAM!")
                );

        // when: the service is called
        try {
            decorated.apply(anyInt());
            fail("Expected the retry to fail after first attempt");
        } catch (Exception e) {
            // then: it should not be retried
            verify(service, times(2)).process(anyInt());
            assertEquals(2, retryEvents.size());
            List<Type> expectedList = List.of(
                    Type.RETRY,
                    Type.IGNORED_ERROR
            );
            assertTrue(expectedList.containsAll(retryEvents));
        }
    }
}

package ratelimiter;

import io.github.resilience4j.core.functions.Either;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.Test;
import service.RemoteService;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RateLimiterTest {

    static Logger logger = Logger.getLogger(RateLimiterTest.class.getName());

    @Test
    public void testRateLimiterNormalBehavior() {
        // given: a remote service
        RemoteService service = mock(RemoteService.class);

        // and: a rate limiter configuration
        int limitForPeriod = 5;
        Duration limitRefreshPeriod = Duration.ofSeconds(1);
        Duration timeoutDuration = Duration.ofMillis(500);
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)
                .limitRefreshPeriod(limitRefreshPeriod)
                .timeoutDuration(timeoutDuration)
                .build();

        // and: a function is decorated with a rate limiter
        RateLimiter rateLimiter = RateLimiter.of("test", config);
        Function<Integer, Integer> decorated = RateLimiter
                .decorateFunction(rateLimiter, service::process);

        // and: logs are placed on all rate limiter events
        logAllRateLimiterEvents(rateLimiter);

        // and: the underlying service is configured to always return success
        when(service.process(anyInt())).thenReturn(0);

        // when: the decorated function is invoked within the limit
        for (int i = 0; i < limitForPeriod; i++) {
            decorated.apply(i);
        }

        // then: the rate limiter allows the calls
        verify(service, times(limitForPeriod)).process(anyInt());

        // when: the decorated function is invoked exceeding the limit
        assertThrows(RequestNotPermitted.class, () -> decorated.apply(limitForPeriod + 1));

        // then: the rate limiter blocks the call
        verify(service, times(limitForPeriod)).process(anyInt());

        // and: after the refresh period
        sleepFor(limitRefreshPeriod.toMillis());

        // then: the rate limiter allows the calls again
        decorated.apply(0);
        verify(service, times(limitForPeriod + 1)).process(anyInt());
    }

    @Test
    public void testRateLimiterTimeout() {
        // given: a remote service
        RemoteService service = mock(RemoteService.class);

        // and: a rate limiter configuration
        int limitForPeriod = 2;
        Duration limitRefreshPeriod = Duration.ofSeconds(1);
        Duration timeoutDuration = Duration.ofMillis(100);
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)
                .limitRefreshPeriod(limitRefreshPeriod)
                .timeoutDuration(timeoutDuration)
                .build();

        // and: a function is decorated with a rate limiter
        RateLimiter rateLimiter = RateLimiter.of("testTimeout", config);
        Function<Integer, Integer> decorated = RateLimiter.decorateFunction(rateLimiter, service::process);

        // and: logs are placed on all rate limiter events
        logAllRateLimiterEvents(rateLimiter);

        // and: the underlying service is configured to always return success
        when(service.process(anyInt())).thenReturn(0);

        // when: the decorated function is invoked, exceeding the limit
        Executors.newSingleThreadExecutor().submit(() -> {
            for (int i = 0; i < limitForPeriod + 1; i++) {
                try {
                    decorated.apply(i);
                } catch (Exception ignore) {
                }
            }
        });

        sleepFor(50L); // Wait for half of the timeout duration

        // then: the rate limiter blocks the call and waits
        assertThrows(RequestNotPermitted.class, () -> decorated.apply(limitForPeriod + 1));

        // and: after the refresh period
        sleepFor(limitRefreshPeriod.toMillis());

        // then: the rate limiter allows the calls again
        decorated.apply(0);
        verify(service, times(limitForPeriod + 1)).process(anyInt());

    }

    @Test
    public void testDynamicLimitChange() {
        // given: a remote service
        RemoteService service = mock(RemoteService.class);

        // and: a rate limiter configuration
        int initialLimitForPeriod = 2;
        Duration limitRefreshPeriod = Duration.ofSeconds(1);
        Duration timeoutDuration = Duration.ofMillis(500);
        RateLimiterConfig initialConfig = RateLimiterConfig.custom()
                .limitForPeriod(initialLimitForPeriod)
                .limitRefreshPeriod(limitRefreshPeriod)
                .timeoutDuration(timeoutDuration)
                .build();

        // and: a rate limiter is created with the initial configuration
        RateLimiter rateLimiter = RateLimiter.of("testDynamic", initialConfig);
        Function<Integer, Integer> decorated = RateLimiter.decorateFunction(rateLimiter, service::process);

        // and: logs are placed on all rate limiter events
        logAllRateLimiterEvents(rateLimiter);

        // and: the underlying service is configured to always return success
        when(service.process(anyInt())).thenReturn(0);

        // when: the decorated function is invoked within the initial limit
        for (int i = 0; i < initialLimitForPeriod; i++) {
            decorated.apply(i);
        }

        // then: the rate limiter allows the calls
        verify(service, times(initialLimitForPeriod)).process(anyInt());

        // when: the limit is dynamically changed
        int newLimitForPeriod = 5;
        rateLimiter.changeLimitForPeriod(newLimitForPeriod);

        // and: some time is allowed for the rate limiter to refresh
        sleepFor(limitRefreshPeriod.toMillis());

        // and: the decorated function is invoked within the new limit
        for (int i = 0; i < newLimitForPeriod; i++) {
            decorated.apply(0);
        }

        // then: the rate limiter allows the calls up to the new limit
        int actualLimitCount = initialLimitForPeriod + newLimitForPeriod;
        verify(service, times(actualLimitCount)).process(anyInt());

        // when: the decorated function is invoked, exceeding the new limit
        assertThrows(Exception.class, () -> decorated.apply(newLimitForPeriod + 1));

        // then: the rate limiter blocks the call
        verify(service, times(actualLimitCount)).process(anyInt());
    }

    @Test
    public void testDrainPermissionsOnServiceResult() {
        // given: a remote service
        RemoteService service = mock(RemoteService.class);

        // and: a rate limiter configuration
        int limitForPeriod = 1500;
        Duration limitRefreshPeriod = Duration.ofSeconds(1);
        Duration timeoutDuration = Duration.ofMillis(500);
        int resultToDrainOn = -1;
        Predicate<Either<? extends Throwable, ?>> drainOnResult = Either -> Either.isRight() && Either.get().equals(resultToDrainOn);
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitForPeriod)
                .limitRefreshPeriod(limitRefreshPeriod)
                .timeoutDuration(timeoutDuration)
                .drainPermissionsOnResult(drainOnResult)
                .build();

        // and: a rate limiter is created
        RateLimiter rateLimiter = RateLimiter.of("testDrain", config);
        Function<Integer, Integer> decorated = RateLimiter.decorateFunction(rateLimiter, service::process);

        // and: logs are placed on all rate limiter events
        logAllRateLimiterEvents(rateLimiter);

        // and: the underlying service is configured to return different results
        when(service.process(0)).thenReturn(resultToDrainOn);
        when(service.process(1)).thenReturn(0);

        // when: the decorated function is invoked successively a few times before exceeding the limit
        int drainOnResultCount = 100;
        for (int i = 0; i < drainOnResultCount; i++) {
            decorated.apply(1);
        }

        // then: the rate limiter allows the calls
        verify(service, times(drainOnResultCount)).process(anyInt());

        // when: the decorated function is invoked with a result that should drain the permissions
        decorated.apply(0);

        // then: the rate limiter blocks the call
        verify(service, times(drainOnResultCount)).process(1);

        // and: after the refresh period
        sleepFor(limitRefreshPeriod.toMillis());

        // then: the rate limiter allows the calls again
        for (int i = 0; i < limitForPeriod; i++) {
            decorated.apply(1);
        }
        verify(service, times(drainOnResultCount + limitForPeriod)).process(1);

    }

    private static void sleepFor(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void logAllRateLimiterEvents(RateLimiter rateLimiter) {
        rateLimiter.getEventPublisher()
                .onEvent(event -> logger.info(event.toString()));
    }
}

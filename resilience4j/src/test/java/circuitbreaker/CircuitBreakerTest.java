package circuitbreaker;

import exceptions.WebServiceException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;
import service.RemoteService;

import java.time.Duration;
import java.util.function.Function;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CircuitBreakerTest {

    static Logger logger = Logger.getLogger(CircuitBreakerTest.class.getName());

    @Test
    public void testCircuitBreakerNormalBehavior() {
        // given: a remote service
        RemoteService service = mock(RemoteService.class);

        // and: a circuit breaker configuration
        int minimumNrOfCalls = 5;
        long waitDurationInOpenState = 2000;
        long maxWaitDurationInHalfOpenState = 0; // should wait indefinitely for all permittedNumberOfCallsInHalfOpenState
        int permittedNumberOfCallsInHalfOpenState = 2;
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .slidingWindow(100, minimumNrOfCalls, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .failureRateThreshold(100) // 100%
                .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                .waitDurationInOpenState(Duration.ofMillis(waitDurationInOpenState))
                .maxWaitDurationInHalfOpenState(Duration.ofMillis(maxWaitDurationInHalfOpenState))
                .build();

        // and: a function is decorated with a circuit breaker
        CircuitBreaker circuitBreaker = CircuitBreaker.of("test", config);
        Function<Integer, Integer> decorated = CircuitBreaker
                .decorateFunction(circuitBreaker, service::process);

        // and: logs are placed on all circuit breaker events
        logAllCircuitBreakerEvents(circuitBreaker);

        // and: the underlying service is configured to always throw an exception
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"));

        // and: before the failure rate threshold is reached, the circuit breaker is closed
        assertSame(CircuitBreaker.State.CLOSED, circuitBreaker.getState());

        // when: the decorated function is invoked minimumNumberOfCalls times
        for (int i = 1; i < minimumNrOfCalls; i++) {
            try {
                decorated.apply(i);
            } catch (Exception ignore) {
                // then: the circuit breaker is in the closed state because the failure rate threshold
                // wasn't yet calculated (which is done after minimumNumberOfCalls)
                assertSame(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
            }
        }

        // when: the decorated function is invoked one more time
        try {
            decorated.apply(anyInt());
        } catch (Exception ignore) {
            // then: the circuit breaker is in the open state
            assertSame(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        }

        // and: after the wait duration in open state
        sleepFor(waitDurationInOpenState + 1000);

        // then: the circuit breaker is in the half-open state
        assertSame(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());

        // and: after the max wait duration in half-open state
        sleepFor(maxWaitDurationInHalfOpenState);

        // and: the underlying service is configured to always return success
        reset(service);
        when(service.process(anyInt()))
                .thenReturn(0);

        for (int i = 0; i < permittedNumberOfCallsInHalfOpenState; i++) {
            // when: the decorated function is invoked
            try {
                decorated.apply(i);
            } catch (Exception ignore) {
                // then: the circuit breaker is in the closed state
                assertSame(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
            }
        }

    }

    @Test
    public void testCircuitBreakerOpenState() {
        // given: a remote service
        RemoteService service = mock(RemoteService.class);

        // and: a circuit breaker configuration
        int failureRateThreshold = 100;
        int minimumNrOfCalls = 10;
        long waitDurationInOpenState = 2000;
        long maxWaitDurationInHalfOpenState = 2000;
        int slidingWindowSize = 10;
        int permittedNumberOfCallsInHalfOpenState = 2;
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindow(slidingWindowSize, minimumNrOfCalls, CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofMillis(waitDurationInOpenState))
                .maxWaitDurationInHalfOpenState(Duration.ofMillis(maxWaitDurationInHalfOpenState))
                .permittedNumberOfCallsInHalfOpenState(permittedNumberOfCallsInHalfOpenState)
                .build();

        // and: a function is decorated with a circuit breaker
        CircuitBreaker circuitBreaker = CircuitBreaker.of("test", config);
        Function<Integer, Integer> decorated = CircuitBreaker
                .decorateFunction(circuitBreaker, service::process);

        // and: logs are placed on all circuit breaker events
        //logAllCircuitBreakerEvents(circuitBreaker);

        // and: the underlying service is configured to always throw an exception
        when(service.process(anyInt()))
                .thenThrow(new WebServiceException("BAM!"));

        // and: before the failure rate threshold is reached, the circuit breaker is closed
        assertSame(CircuitBreaker.State.CLOSED, circuitBreaker.getState());

        // when: the decorated function is invoked minimumNumberOfCalls times
        for (int i = 1; i < minimumNrOfCalls; i++) {
            try {
                decorated.apply(i);
            } catch (Exception ignore) {
                // then: the circuit breaker is in the closed state because the failure rate threshold
                // wasn't yet calculated (which is done after minimumNumberOfCalls)
                assertSame(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
            }
        }

        // when: the decorated function is invoked one more time
        try {
            decorated.apply(anyInt());
        } catch (Exception ignore) {
            // then: the circuit breaker is in the open state
            assertSame(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        }

        // and: after the wait duration in open state
        sleepFor(waitDurationInOpenState + 1000);

        // then: the circuit breaker is still in the open state (because automatic transition is disabled)
        assertSame(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        // when: the decorated function is invoked once
        try {
            decorated.apply(anyInt());
        } catch (Exception ignore) {
            // then: the circuit breaker is in the HALF_OPEN state
            assertSame(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());
        }

        // when: the service is configured to always return success
        reset(service);
        when(service.process(anyInt()))
                .thenReturn(0);

        // and: the decorated function is invoked the nr of times necessary to lower the failure rate below the threshold
        for (int i = 0; i < permittedNumberOfCallsInHalfOpenState; i++) {
            try {
                decorated.apply(anyInt());
            } catch (Exception ignore) {
                // ignore
            } finally {
                System.out.println(circuitBreaker.getState());
            }
        }

        // then: the circuit breaker is in the closed state
        assertSame(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    // TODO: slow call threshold, manual state transition, time-based sliding window

    private static void sleepFor(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void logAllCircuitBreakerEvents(CircuitBreaker circuitBreaker) {
        circuitBreaker.getEventPublisher()
                .onEvent(event -> logger.info(event.toString()));
    }
}

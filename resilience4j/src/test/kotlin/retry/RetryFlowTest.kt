package retry

import RemoteFailingService
import exceptions.NetworkException
import exceptions.ReachedLastIntegerException
import io.github.resilience4j.kotlin.retry.RetryConfig
import io.github.resilience4j.kotlin.retry.RetryRegistry
import io.github.resilience4j.kotlin.retry.addRetryConfig
import io.github.resilience4j.kotlin.retry.executeFunction
import io.github.resilience4j.kotlin.retry.executeSuspendFunction
import io.github.resilience4j.kotlin.retry.retry
import io.github.resilience4j.retry.Retry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import utils.repeat
import java.time.Duration
import java.util.logging.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import io.github.resilience4j.retry.Retry as RetryJava
import io.github.resilience4j.retry.RetryConfig as RetryConfigJava

class RetryFlowTest {

    companion object {
        private val logger = Logger.getLogger(RetryFlowTest::class.java.name)
        private const val ONE_SECOND = 1000L
    }

    private val ints = listOf(1, 2, 3, 4, 5)

    @Test
    fun `retry a flow collection`() {
        // given: a retry configuration
        val maxAttempts = 2
        val retryConfig = RetryConfigJava.custom<Any>()
            .maxAttempts(maxAttempts)
            .retryExceptions(ReachedLastIntegerException::class.java)
            .build()

        // and: a registered retry instance
        val retry = RetryJava.of("retry", retryConfig)

        // and: an empty list to collect the data from the flow
        val flowContainer = mutableListOf<Int>()

        runBlocking {
            assertFailsWith<ReachedLastIntegerException> {
                // and: a flow of data
                flowOf(*ints.toTypedArray())
                    // and: a some transformation that could lead to an error
                    .map { processNumber(it, flowContainer) }
                    // with: a retry operator
                    .retry(retry) // uses .retryWhen under the hood
                    // when: a terminal operator is used to start the flow collection
                    .toList()
            }
        }
        
        // then: the flow collection should be retried the number of times specified in the retry configuration
        assertEquals(ints.repeat(maxAttempts), flowContainer)

    }

    private suspend fun processNumber(num: Int, list: MutableList<Int>): Int {
        logger.info("value: $num")
        list.add(num)
        if (num >= ints.last()) {
            logger.info("Reached last integer")
            throw ReachedLastIntegerException()
        }
        delay(ONE_SECOND) // simulate process time
        return num
    }

    @Test
    fun `decorate a suspend function with retry`() {
        // given: a retry configuration
        val maxAttempts = 5
        val retryConfig = RetryConfig {
            maxAttempts(maxAttempts)
            waitDuration(Duration.ofMillis(10))
        }

        // and: a retry instance
        val retry = Retry.of("custom-suspend") { retryConfig }

        // and: a remote service
        val remoteFailingService = RemoteFailingService()
        assertFailsWith<NetworkException> {
            runBlocking {

                // when: a suspend operation which fails is decorated and executed with retry
                retry.executeSuspendFunction {
                    remoteFailingService.suspendCall()
                }

            }
        }

        // then: the remote service should be called the number of times specified in the retry configuration
        assertEquals(maxAttempts, remoteFailingService.invocationCounter)

    }

    @Test
    fun `decorate a function using retry`() {
        // given: a retry configuration
        val maxAttempts = 5
        val retryConfig = RetryConfig {
            maxAttempts(maxAttempts)
            waitDuration(Duration.ofMillis(10))
        }

        // and: a retry instance
        val retry = Retry.of("custom-blocking") { retryConfig }

        // and: a remote service
        val remoteFailingService = RemoteFailingService()
        assertFailsWith<NetworkException> {

            // when: a blocking operation which fails is decorated and executed with retry
            retry.executeFunction {
                remoteFailingService.blockingCall()
            }
        }

        // then: the remote service should be called the number of times specified in the retry configuration
        assertEquals(maxAttempts, remoteFailingService.invocationCounter)
    }

    @Test
    fun `use base configuration in a retry instance`() {
        // given: a base retry configuration
        val baseMaxAttempts = 5
        val baseRetryConfig = RetryConfig {
            maxAttempts(baseMaxAttempts)
        }

        // and: a retry registry
        val singularMaxAttempts = 10
        val retryRegistry = RetryRegistry {

            // when: a retry configuration is added to the registry using the base configuration
            addRetryConfig("using-base", baseRetryConfig) {
                retryOnResult { true }
            }

            // and: a retry configuration is added to the registry without using the base configuration
            addRetryConfig("singular") {
                maxAttempts(singularMaxAttempts)
            }
        }

        // and: two retry instances are created using both registered configurations
        val retryBase = retryRegistry.retry("retry-base", "using-base")
        val retrySingular = retryRegistry.retry("retry-singular", "singular")

        // then: the retry instance that uses the base configuration should have the base
        // configuration values if not overridden
        assertEquals(baseMaxAttempts, retryBase.retryConfig.maxAttempts)

        // and: the retry instance that does not use the base configuration should have the
        // values specified in its configuration
        assertEquals(singularMaxAttempts, retrySingular.retryConfig.maxAttempts)

    }

}


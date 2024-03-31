import exceptions.NetworkException
import kotlinx.coroutines.delay
import service.RemoteService
import java.util.logging.Logger

class RemoteFailingService {

    companion object {
        private val logger = Logger.getLogger(RemoteService::class.java.name)
        private const val ONE_SECOND = 1000L
    }

    var invocationCounter = 0
        private set

    private val msg = "Calling a remote service which will fail"
    private val exceptionMsg = "Remote service failed"

    suspend fun suspendCall() {
        invocationCounter++
        logger.info(msg)
        delay(ONE_SECOND) // simulate network delay
        throw NetworkException(exceptionMsg)
    }

    fun blockingCall() {
        invocationCounter++
        logger.info(msg)
        Thread.sleep(ONE_SECOND) // simulate network delay
        throw NetworkException(exceptionMsg)
    }


}
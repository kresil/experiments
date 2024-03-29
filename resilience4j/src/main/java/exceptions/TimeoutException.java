package exceptions;

public final class TimeoutException extends RemoteServiceException {
    public TimeoutException(String message) {
        super(message);
    }
}

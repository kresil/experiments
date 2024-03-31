package exceptions;

public final class NetworkException extends RemoteServiceException {
    public NetworkException(String message) {
        super(message);
    }
}

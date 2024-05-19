package exceptions;

public sealed class RemoteServiceException extends RuntimeException permits WebServiceException, NetworkException {
    public RemoteServiceException(String message) {
        super(message);
    }
}


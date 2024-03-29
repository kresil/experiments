package exceptions;

// TODO: Why cant it extend only Exception?
public sealed class RemoteServiceException extends RuntimeException permits WebServiceException, TimeoutException, NetworkException {
    public RemoteServiceException(String message) {
        super(message);
    }
}


package exceptions;

public final class WebServiceException extends RemoteServiceException {
    public WebServiceException(String message) {
        super(message);
    }
}

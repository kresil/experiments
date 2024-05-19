package exceptions;

public class BusinessServiceException extends RuntimeException {
    public BusinessServiceException(String message) {
        super(message);
    }
}


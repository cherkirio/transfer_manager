package model.exception;

/**
 * Created by kirio on 30.10.2018.
 */
public class BadParamException extends RuntimeException {
    public BadParamException(String message) {
        super(message);
    }
}

package exceptions;

public class SubscriptionExistsException extends Exception {

    public SubscriptionExistsException() {
    }

    public SubscriptionExistsException(String message) {
        super(message);
    }
}

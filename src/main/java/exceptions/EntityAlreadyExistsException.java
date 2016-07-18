package exceptions;

/**
 * Created by Crafton Williams on 9/06/2016.
 */
public class EntityAlreadyExistsException extends Exception {

    public EntityAlreadyExistsException() {
    }

    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}

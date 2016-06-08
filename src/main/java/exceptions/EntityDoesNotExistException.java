package exceptions;

/**
 * Created by Crafton Williams on 8/06/2016.
 */
public class EntityDoesNotExistException extends Exception {

    public EntityDoesNotExistException() {
    }

    public EntityDoesNotExistException(String message) {
        super(message);
    }
}

package exceptions;

/**
 * Created by Crafton Williams on 8/06/2016.
 */
public class EntityBeingUsedException extends Exception {

    public EntityBeingUsedException() {
    }

    public EntityBeingUsedException(String message) {
        super(message);
    }
}

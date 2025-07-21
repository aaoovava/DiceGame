package exceptions;

/**
 * Exception for missing game fields
 */
public class SomeGameFieldsMissing extends Exception {

    public SomeGameFieldsMissing(String message) {
        super(message);
    }
}

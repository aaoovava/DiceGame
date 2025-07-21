package exceptions;

/**
 * Exception for wrong probabilities
 */
public class WrongProbavilitiesSetUp extends IllegalArgumentException{
    public WrongProbavilitiesSetUp(String message) {
        super(message);
    }
}

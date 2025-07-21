package model.records;

import model.records.dice.Dice;
import model.records.enums.EndingOfTurn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a turn in the game, including the score, the result of the turn,
 * the selected dice, the remaining dice, and the displayed dice.
 *
 * <p>This class is used to store the details of each player's turn in the game, including
 * the score achieved, the selected dice, and the remaining dice after the turn ends.</p>
 */
public class Turn {
    private final int turnScore;
    private final EndingOfTurn endingOfTurn;
    private final List<Dice> selectedDice;
    private final List<Dice> remainingDice;
    private final List<Dice> displayedDice;

    /**
     * Constructs a new Turn instance.
     *
     * @param turnScore the accumulated score for this turn
     * @param endingOfTurn the result of the turn (e.g., SCORED, PASS, BUSTED)
     * @param selectedDice the list of dice selected during the turn
     * @param remainingDice the list of dice remaining after the turn
     * @param displayedDice the list of dice displayed after the turn
     */
    public Turn(int turnScore, EndingOfTurn endingOfTurn,
                List<Dice> selectedDice, List<Dice> remainingDice, List<Dice> displayedDice) {
        this.turnScore = turnScore;
        this.endingOfTurn = endingOfTurn;
        this.selectedDice = new ArrayList<>(selectedDice);
        this.remainingDice = new ArrayList<>(remainingDice);
        this.displayedDice = new ArrayList<>(displayedDice);
    }

    /**
     * Gets the accumulated score for this turn.
     *
     * @return the accumulated score for the turn
     */
    public int getTurnScore() {
        return turnScore;
    }

    /**
     * Gets the result of the turn (e.g., SCORED, PASS, BUSTED).
     *
     * @return the result of the turn
     */
    public EndingOfTurn getEndingOfTurn() {
        return endingOfTurn;
    }

    /**
     * Gets an unmodifiable list of the dice selected during the turn.
     *
     * @return an unmodifiable list of selected dice
     */
    public List<Dice> getSelectedDice() {
        return selectedDice;
    }

    /**
     * Gets an unmodifiable list of the dice remaining after the turn.
     *
     * @return an unmodifiable list of remaining dice
     */
    public List<Dice> getRemainingDice() {
        return remainingDice;
    }

    /**
     * Returns a string representation of the Turn, including the score, result,
     * number of selected dice, and number of remaining dice.
     *
     * @return a string representation of the Turn
     */
    @Override
    public String toString() {
        return String.format("Turn[score=%d, result=%s, selected=%d dice, remaining=%d dice]",
                turnScore, endingOfTurn, selectedDice.size(), remainingDice.size());
    }

    /**
     * Gets the list of dice displayed at the end of the turn.
     *
     * @return the list of displayed dice
     */
    public List<Dice> getDisplayedDice() {
        return displayedDice;
    }
}
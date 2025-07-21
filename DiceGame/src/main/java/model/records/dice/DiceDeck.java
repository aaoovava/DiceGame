package model.records.dice;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a collection of dice, referred to as a deck,
 * that can be rolled together.
 *
 * <p>This class provides functionality to manage a deck of dice, including
 * adding/removing dice, calculating the total balance of the dice in the deck,
 * and rolling all the dice in the deck.</p>
 */
public class DiceDeck implements Serializable {
    private static final long serialVersionUID = 1L;

    // The total balance of all dice in the deck
    private int balance;

    // List of dice in the deck
    private List<Dice> deck;

    /**
     * Constructs a new DiceDeck with the given list of dice.
     *
     * @param deck a list of {@link Dice} objects to initialize the deck
     */
    public DiceDeck(List<Dice> deck) {
        this.deck = deck;
    }

    /**
     * Calculates and returns the total balance of all the dice in the deck.
     *
     * <p>This method sums up the balance values of each dice in the deck and
     * returns the total balance.</p>
     *
     * @return the total balance of the dice in the deck
     */
    public int getBalance() {
        balance = 0;
        for (Dice dice : deck) {
            balance += dice.getBalance();
        }
        return balance;
    }

    /**
     * Adds a new dice to the deck.
     *
     * @param dice the {@link Dice} object to be added to the deck
     */
    public void addDice(Dice dice) {
        deck.add(dice);
    }

    /**
     * Removes the specified dice from the deck.
     *
     * @param dice the {@link Dice} object to be removed from the deck
     */
    public void removeDice(Dice dice) {
        deck.remove(dice);
    }

    /**
     * Rolls all the dice in the deck.
     *
     * <p>This method invokes the {@link Dice#roll()} method on each dice in
     * the deck, simulating a roll for all dice.</p>
     */
    public void roll() {
        for (Dice dice : deck) {
            dice.roll();
        }
    }

    /**
     * Returns the list of dice currently in the deck.
     *
     * @return a list of {@link Dice} objects in the deck
     */
    public List<Dice> getDeck() {
        return deck;
    }

    /**
     * Sets the list of dice for the deck.
     *
     * @param deck the list of {@link Dice} objects to set for the deck
     */
    public void setDeck(List<Dice> deck) {
        this.deck = deck;
    }
}
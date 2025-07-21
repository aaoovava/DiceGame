package model.records.dice;

import model.Game;

/**
 * A specialized dice class that extends `RiskDice` and introduces a special behavior for rolling a 1,
 * granting double points but also applying a penalty to the balance.
 * The class provides specific logic for handling the risk condition and its associated effects.
 */
public class RoyalDice extends RiskDice {

    /**
     * Constructor for creating a RoyalDice instance with a specific side.
     * Initializes the dice with custom values and special risk behavior.
     *
     * @param side The side of the dice to initialize with
     */
    public RoyalDice(int side) {
        super(side);
        setName("RoyalDice");
        setBalance(-1); // Sets the balance penalty when rolled
        setPrice(Integer.parseInt("200")); // Sets the price of the dice
        setInfo("A regal die with a dangerous power—rolling a 1 grants double points, but its unpredictability comes with a -1 balance penalty.\n" +
                "\n");
        this.setRiskNumber(1); // The risk condition occurs when the dice lands on 1
    }

    /**
     * Override method to check if the risk number (1) is dropped and trigger a special action.
     * If the risk number is rolled, the game will display a special message.
     *
     * @return true if the risk number was rolled and the special action was triggered, false otherwise
     */
    @Override
    public boolean riskNumberDropped() {
        if (super.riskNumberDropped()) {
            Game.getInstance().showRoyalDiceMessage(); // Shows a special message for the royal dice
            return true;
        }
        return false;
    }

    /**
     * Returns the image name associated with the current side of the RoyalDice.
     *
     * @return A string representing the image file name for the current side of the RoyalDice
     */
    @Override
    public String returnImageName() {
        return "/img/royal_dice/rd" + this.getCurrentSide() + ".png"; // Path to the image for the current side
    }
}
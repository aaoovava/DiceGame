package model.records.dice;

/**
 * Represents a special type of dice that has an associated risk number.
 * The risk number determines a specific value that, when rolled, triggers a "risk" event.
 *
 * <p>This class extends the {@link Dice} class and adds a mechanism to track a "risk number",
 * which is compared to the current side of the dice during a roll.</p>
 */
public class RiskDice extends Dice {
    // The risk number that triggers a specific event when rolled
    private int riskNumber;

    /**
     * Constructs a new RiskDice object with the given number of sides.
     *
     * @param side the number of sides of the dice
     */
    public RiskDice(int side) {
        super(side);
    }

    /**
     * Returns the image file name for the current side of the RiskDice.
     *
     * <p>The image name corresponds to the rolled side of the dice and is used
     * for display purposes in the user interface.</p>
     *
     * @return the image file path for the current side of the dice (e.g., "/img/royal_dice/rd1.png")
     */
    @Override
    public String returnImageName() {
        return "/img/royal_dice/rd" + this.getCurrentSide() + ".png";
    }

    /**
     * Checks if the current side of the dice matches the risk number.
     *
     * <p>If the current side of the dice equals the risk number, this method returns true,
     * indicating a "risk" event. Otherwise, it returns false.</p>
     *
     * @return true if the current side matches the risk number, false otherwise
     */
    public boolean riskNumberDropped() {
        if (this.getCurrentSide() == this.getRiskNumber()) {
            return true;
        }
        return false;
    }

    /**
     * Gets the current risk number for this RiskDice.
     *
     * @return the risk number associated with the dice
     */
    public int getRiskNumber() {
        return riskNumber;
    }

    /**
     * Sets the risk number for this RiskDice.
     *
     * @param riskNumber the new risk number to be set
     */
    public void setRiskNumber(int riskNumber) {
        this.riskNumber = riskNumber;
    }
}
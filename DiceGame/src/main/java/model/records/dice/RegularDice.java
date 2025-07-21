package model.records.dice;

/**
 * RegularDice represents a standard, fair die with equal probabilities for each side.
 * It provides a basic implementation for rolling and returning an image name based on the rolled side.
 */
public class RegularDice extends Dice {

    /**
     * Constructor for the RegularDice.
     * Sets the name, description, and price of the dice.
     *
     * @param side the side of the dice to initialize with
     */
    public RegularDice(int side) {
        super(side);
        setName("RegularDice");
        setInfo("A classic, fair die for traditional gameplay.");
        setPrice(0);
    }

    /**
     * Rolls the die. The default behavior is inherited from the Dice class.
     *
     * @see Dice#roll()
     */
    @Override
    public void roll() {
        super.roll();
    }

    /**
     * Returns the image file name for the current side of the dice.
     * The image name is based on the side number and the image directory.
     *
     * @return a string representing the image file name for the current dice side
     */
    @Override
    public String returnImageName() {
        return "/img/dice_normal/n" + getCurrentSide() + ".png";
    }
}
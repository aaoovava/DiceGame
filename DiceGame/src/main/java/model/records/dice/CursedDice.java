package model.records.dice;

import model.Game;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * CursedDice is a custom dice class that modifies the probabilities of rolling certain values.
 * It provides a unique behavior where the numbers 1, 5 have reduced chances of appearing,
 * while 2, 3, 4, and 6 have increased chances.
 */
public class CursedDice extends Dice {

    // Custom probabilities that make 2,3,4,6 more likely (25% each)
    // while 1 and 5 have reduced chance (10% each)
    private static final Map<Integer, Double> CURSED_PROBABILITIES;
    static {
        Map<Integer, Double> probs = new HashMap<>();
        probs.put(1, 0.10);  // 10% chance for 1 (reduced from ~16.67%)
        probs.put(2, 0.20);   // 25% chance for 2
        probs.put(3, 0.20);   // 25% chance for 3
        probs.put(4, 0.20);   // 25% chance for 4
        probs.put(5, 0.10);   // 10% chance for 5 (reduced from ~16.67%)
        probs.put(6, 0.20);   // 25% chance for 6
        CURSED_PROBABILITIES = Map.copyOf(probs);
    }

    /**
     * Constructor for the CursedDice.
     * Sets the dice name, balance, price, and provides a custom probability map for the dice sides.
     *
     * @param side the side of the dice to initialize with
     */
    public CursedDice(int side) {
        super(side);
        setName("CursedDice");
        setBalance(2);
        setPrice(10);
        setInfo("A sinister die that avoids extremes, making 1s and 5s rare but rewarding with a +2 bonus to balance");
        this.setCustomProbabilities(CURSED_PROBABILITIES);
    }

    /**
     * Returns the image file name for the current side of the dice.
     * The image name is based on the side number and the image directory.
     *
     * @return a string representing the image file name for the current dice side
     */
    @Override
    public String returnImageName() {
        return "/img/cursed_dice/cd" + this.getCurrentSide() + ".png";
    }

    //img/cursed_dice/cd3.png

}
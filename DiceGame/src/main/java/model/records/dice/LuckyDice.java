package model.records.dice;

import model.Game;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a special type of {@link Dice} called LuckyDice.
 *
 * <p>LuckyDice favors landing on sides 1 and 5 more often,
 * each with a 30% probability instead of the usual 16.67% in a fair dice roll.
 * Other sides have a 10% chance each.
 * This makes LuckyDice useful for strategies relying on scoring 1s and 5s.</p>
 *
 * <p>It comes with a cost of -1 balance per roll and has a predefined
 * price of 20 units in the game.</p>
 */
public class LuckyDice extends Dice {

    /**
     * Custom probabilities for LuckyDice.
     * 1 and 5 have a 30% chance, while 2, 3, 4, and 6 each have a 10% chance.
     */
    private static final Map<Integer, Double> LUCKY_PROBABILITIES;

    // Static block to initialize LUCKY_PROBABILITIES
    static {
        Map<Integer, Double> probs = new HashMap<>();
        probs.put(1, 0.30);
        probs.put(2, 0.10);
        probs.put(3, 0.10);
        probs.put(4, 0.10);
        probs.put(5, 0.30);
        probs.put(6, 0.10);
        LUCKY_PROBABILITIES = Map.copyOf(probs);
    }

    /**
     * Constructs a new LuckyDice with the given number of sides.
     *
     * @param side the number of sides for the dice (typically 6)
     */
    public LuckyDice(int side) {
        super(side);
        setName("LuckyDice");
        setPrice(20);
        setBalance(-1);
        setInfo("Favors fortune, landing on 1 or 5 more often—but at a small cost (-1) for balance.");
        this.setCustomProbabilities(LUCKY_PROBABILITIES);
    }

    /**
     * Rolls the LuckyDice using the custom probability distribution.
     */
    @Override
    public void roll() {
        super.roll(); // Delegates to parent's roll method with custom probabilities
    }

    /**
     * Returns the image path corresponding to the current side of the LuckyDice.
     *
     * @return the relative path to the LuckyDice image.
     */
    @Override
    public String returnImageName() {
        return "/img/lucky_dice/ld" + this.getCurrentSide() + ".png";
    }
}
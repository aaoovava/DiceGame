package services;

import model.records.dice.Dice;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreCalculatorService {
    private static ScoreCalculatorService instance;

    // Private constructor for Singleton pattern
    private ScoreCalculatorService() {}

    public static synchronized ScoreCalculatorService getInstance() {
        if (instance == null) {
            instance = new ScoreCalculatorService();
        }
        return instance;
    }

    /**
     * Main method to calculate score
     * @param diceList list of dice
     * @return score based on Farkle rules
     */
    public int calculateScore(List<Dice> diceList) {
        if (diceList == null || diceList.isEmpty()) return 0;

        Map<Integer, Integer> frequencyMap = createFrequencyMap(diceList);

        // Check special combinations for 6 dice
        if (diceList.size() == 6) {
            int specialScore = checkSpecialCombinations(frequencyMap);
            if (specialScore > 0) return specialScore;
        }

        // Validate that all non-1/5 dice are part of a scoring combination
        if (!validateAllDice(frequencyMap)) return 0;

        // Standard score calculation
        return calculateStandardScores(frequencyMap);
    }

    /**
     * Validates that all non-1/5 dice participate in scoring combinations
     */
    private boolean validateAllDice(Map<Integer, Integer> freqMap) {
        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            int value = entry.getKey();
            int count = entry.getValue();

            if (value == 1 || value == 5) continue;
            if (count < 3) return false;
        }
        return true;
    }

    /**
     * Creates a frequency map of dice face values
     */
    private Map<Integer, Integer> createFrequencyMap(List<Dice> diceList) {
        return diceList.stream()
                .collect(Collectors.groupingBy(
                        Dice::getCurrentSide,
                        Collectors.summingInt(e -> 1)
                ));
    }

    /**
     * Checks for special combinations with 6 dice
     */
    private int checkSpecialCombinations(Map<Integer, Integer> freqMap) {
        // Straight (1 through 6)
        if (freqMap.size() == 6) return 1500;

        // Three pairs
        if (freqMap.size() == 3 && freqMap.values().stream().allMatch(c -> c == 2))
            return 1500;

        // Four of a kind + a pair
        if (freqMap.containsValue(4) && freqMap.containsValue(2))
            return 1500;

        // Two sets of three of a kind
        if (freqMap.values().stream().allMatch(c -> c == 3))
            return 2500;

        return 0;
    }

    /**
     * Standard score calculation
     */
    private int calculateStandardScores(Map<Integer, Integer> freqMap) {
        int score = 0;

        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            int value = entry.getKey();
            int count = entry.getValue();

            if (value == 1) {
                score += 1000 * (count / 3); // Base triple of 1s
                score += 100 * (count % 3);  // Extra single 1s
            }
            else if (value == 5) {
                score += 500 * (count / 3);  // Base triple of 5s
                score += 50 * (count % 3);   // Extra single 5s
            }
            else if (count >= 3) {
                score += value * 100 * (count / 3); // Triples of other values

                // Bonus for more than 3 of a kind
                if (count >= 4) {
                    score += 1000 * ((count - 3)); // +1000 per extra die
                }
            }
        }
        return score;
    }

    /**
     * Checks if the player has any scoring combination
     * (i.e. not busted after the roll)
     * @param diceList list of dice
     * @return true if there is at least one scoring combination, false otherwise
     */
    public boolean hasAnyScoringCombination(List<Dice> diceList) {
        if (diceList == null || diceList.isEmpty()) return false;

        Map<Integer, Integer> frequencyMap = createFrequencyMap(diceList);

        // Check special combinations for 6 dice
        if (diceList.size() == 6 && checkSpecialCombinations(frequencyMap) > 0)
            return true;

        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            int value = entry.getKey();
            int count = entry.getValue();

            if (value == 1 || value == 5) {
                return true; // Single 1s or 5s always give points
            }

            if (count >= 3) {
                return true; // Any triple or more gives points
            }
        }

        return false; // No scoring combination found
    }
}
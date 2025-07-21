package model.records.npc;

import model.Game;
import model.records.Turn;
import model.records.dice.Dice;
import model.records.dice.DiceDeck;
import model.records.enums.EndingOfTurn;
import services.ScoreCalculatorService;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * NPC (Non-Player Character) class represents an automated player in the dice game.
 * This class is responsible for handling the behavior of NPCs, such as rolling dice,
 * simulating turns, making decisions based on different strategies, and calculating the
 * optimal moves based on difficulty and other parameters.
 *
 * <p>The NPC uses a Monte Carlo simulation approach to simulate and evaluate different
 * combinations of dice rolls. The class can adapt its behavior based on the difficulty
 * level and performs actions accordingly.
 */
public class NPC extends Player {
    private static final int MAX_DEPTH = 1;
    private static final int MAX_COMBINATIONS = 100;
    private static final int BASE_TIMEOUT = 4;

    // Game state
    private List<Dice> currentRoll = new ArrayList<>();
    private List<Dice> selectedDice = new ArrayList<>();

    private List<Dice> displayedDice = new ArrayList<>();
    private int currentTurnScore = 0;
    private Turn lastTurn;

    // NPC parameters
    private int difficulty;
    private final Boolean integrity;
    private final int maxBet;
    private final int minBet;
    private final String description;
    private final String quote;

    // Services
    private final ScoreCalculatorService scoreService = ScoreCalculatorService.getInstance();
    private final ExecutorService executor = Executors.newWorkStealingPool();
    private final Map<String, Turn> simulationCache = new ConcurrentHashMap<>();

    /**
     * Constructs an NPC player.
     *
     * @param name        the name of the NPC
     * @param diceDeck    the dice deck used by the NPC
     * @param balance     the initial balance of the NPC
     * @param currentBet  the current bet amount of the NPC
     * @param difficulty  the difficulty level of the NPC
     * @param integrity   whether the NPC has integrity (honest play or not)
     * @param maxBet      the maximum bet allowed for the NPC
     * @param minBet      the minimum bet allowed for the NPC
     * @param description the description of the NPC
     * @param quote       the quote associated with the NPC
     */
    public NPC(String name, DiceDeck diceDeck, int balance, int currentBet,
               int difficulty, Boolean integrity, int maxBet, int minBet, String description, String quote) {
        super(name, diceDeck, balance, currentBet);
        this.difficulty = difficulty;
        this.integrity = integrity;
        this.maxBet = maxBet;
        this.minBet = minBet;
        this.description = description;
        this.quote = quote;
    }

    /**
     * Rolls the dice for the NPC and initiates the turn based on the available dice.
     *
     * @param availableDice a list of dice available for the roll
     */
    public void rollDice(List<Dice> availableDice) {
        resetTurnState();

        if (availableDice == null || availableDice.isEmpty()) {
            availableDice = Game.getInstance().getRolledDice();
        }



        currentRoll = new ArrayList<>(availableDice);
        displayedDice = new ArrayList<>(availableDice);
        makeTurn();
    }

    /**
     * Simulates the best turn the NPC can make based on the available dice.
     */
    private void makeTurn() {
        Turn bestTurn = simulateBestTurn(currentRoll, MAX_DEPTH);
        applyTurnResult(bestTurn);
    }

    /**
     * Simulates the best possible turn using a Monte Carlo approach by evaluating various dice combinations.
     *
     * @param availableDice the dice available to the NPC
     * @param depth         the depth of the simulation
     * @return the best possible turn
     */
    private Turn simulateBestTurn(List<Dice> availableDice, int depth) {
        try {
            if (availableDice == null || availableDice.isEmpty()) {
                return createBustedTurn(availableDice);
            }

            String cacheKey = createCacheKey(availableDice, depth);
            Turn cached = simulationCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }

            Set<List<Dice>> validCombinations = generateValidCombinations(availableDice);
            if (validCombinations.isEmpty()) {
                return createBustedTurn(availableDice);
            }

            List<CompletableFuture<Turn>> futures = validCombinations.stream()
                    .limit(MAX_COMBINATIONS)
                    .map(combination -> CompletableFuture.supplyAsync(
                            () -> simulateTurn(combination, new ArrayList<>(availableDice), depth),
                            executor
                    ).orTimeout(calculateTimeout(depth), TimeUnit.SECONDS))
                    .toList();

            Turn bestTurn = createBustedTurn(availableDice);
            for (CompletableFuture<Turn> future : futures) {
                try {
                    Turn current = future.get();
                    if (current.getTurnScore() > bestTurn.getTurnScore()) {
                        bestTurn = current;
                    }
                } catch (Exception e) {
                    System.err.println("[WARN] Skipping combination due to error: " + e.getMessage());
                }
            }

            simulationCache.put(cacheKey, bestTurn);
            return bestTurn;

        } catch (Exception e) {
            System.err.println("[ERROR] Simulation error: " + e.getMessage());
            return createBustedTurn(availableDice);
        }
    }

    /**
     * Creates a unique cache key for a given dice combination and depth level.
     *
     * @param dice  the dice in the combination
     * @param depth the depth of the simulation
     * @return a unique cache key
     */
    private String createCacheKey(List<Dice> dice, int depth) {
        return dice.stream()
                .map(d -> String.valueOf(d.getCurrentSide()))
                .sorted()
                .collect(Collectors.joining("-")) + "|d" + depth;
    }

    /**
     * Calculates the timeout for the simulation based on the depth of the simulation.
     *
     * @param depth the depth of the simulation
     * @return the timeout in seconds
     */
    private long calculateTimeout(int depth) {
        return BASE_TIMEOUT * (MAX_DEPTH - depth + 1);
    }

    /**
     * Creates a busted turn (invalid turn) when the NPC cannot make a valid move.
     *
     * @param dice the list of dice that were rolled
     * @return a busted turn
     */
    private Turn createBustedTurn(List<Dice> dice) {
        return new Turn(0, EndingOfTurn.BUSTED,
                Collections.emptyList(),
                dice != null ? new ArrayList<>(dice) : new ArrayList<>(),displayedDice);
    }

    /**
     * Generates valid combinations of dice rolls based on the available dice.
     *
     * @param dice the list of dice to generate combinations from
     * @return a set of valid dice combinations
     */
    private Set<List<Dice>> generateValidCombinations(List<Dice> dice) {
        Set<List<Dice>> combinations = new LinkedHashSet<>();

        // Сначала обрабатываем комбинации с наибольшим количеством кубиков
        addTripleAndMoreCombinations(dice, combinations);
        addSingleAndPairedCombinations(dice, combinations);

        return combinations.stream()
                .filter(comb -> scoreService.calculateScore(comb) > 0)
                .sorted((a, b) -> Integer.compare(
                        scoreService.calculateScore(b),
                        scoreService.calculateScore(a)
                ))
                .limit(MAX_COMBINATIONS)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Adds combinations of dice that consist of singles and pairs to the list of valid combinations.
     *
     * @param dice        the list of dice available for the combination
     * @param combinations the set of valid combinations to which new combinations will be added
     */
    private void addSingleAndPairedCombinations(List<Dice> dice, Set<List<Dice>> combinations) {
        List<Dice> scoringSingles = dice.stream()
                .filter(d -> d.getCurrentSide() == 1 || d.getCurrentSide() == 5)
                .collect(Collectors.toList());

        for (int i = 1; i <= Math.min(scoringSingles.size(), 3); i++) {
            combinations.addAll(generateCombinations(scoringSingles, i));
        }
    }

    /**
     * Adds combinations of dice that consist of triples or more to the list of valid combinations.
     *
     * @param dice        the list of dice available for the combination
     * @param combinations the set of valid combinations to which new combinations will be added
     */
    private void addTripleAndMoreCombinations(List<Dice> dice, Set<List<Dice>> combinations) {
        Map<Integer, List<Dice>> grouped = dice.stream()
                .collect(Collectors.groupingBy(Dice::getCurrentSide));

        grouped.values().stream()
                .filter(group -> group.size() >= 3)
                .forEach(group -> {
                    for (int i = group.size(); i >= 3; i--) {
                        combinations.addAll(generateCombinations(group, i));
                    }
                });
    }

    /**
     * Generates all possible combinations of dice of a given size.
     *
     * @param dice the list of dice to generate combinations from
     * @param size the size of the combinations to generate
     * @return a set of all possible combinations
     */
    private Set<List<Dice>> generateCombinations(List<Dice> dice, int size) {
        Set<List<Dice>> result = new HashSet<>();
        generateCombinationsHelper(dice, size, 0, new ArrayList<>(), result);
        return result;
    }

    /**
     * Helper method to recursively generate all combinations of dice.
     *
     * @param dice    the list of dice to generate combinations from
     * @param size    the size of the combinations to generate
     * @param start   the starting index for generating combinations
     * @param current the current combination being formed
     * @param result  the set to store the valid combinations
     */
    private void generateCombinationsHelper(List<Dice> dice, int size, int start,
                                            List<Dice> current, Set<List<Dice>> result) {
        if (current.size() == size) {
            if (scoreService.calculateScore(current) > 0) {
                result.add(new ArrayList<>(current));
            }
            return;
        }

        for (int i = start; i < dice.size(); i++) {
            current.add(dice.get(i));
            generateCombinationsHelper(dice, size, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    /**
     * Simulates a turn based on the selected dice and the remaining dice.
     *
     * @param selected      the list of selected dice for the turn
     * @param remainingDice the list of remaining dice that have not been selected
     * @param depth         the depth of the simulation
     * @return the result of the simulated turn
     */
    private Turn simulateTurn(List<Dice> selected, List<Dice> remainingDice, int depth) {
        if (remainingDice.isEmpty()) {
            remainingDice = getDiceDeck().getDeck();
        }
        if (selected.isEmpty() || scoreService.calculateScore(selected) == 0) {
            return createBustedTurn(remainingDice);
        }

        for (Dice dice: selected) {
            remainingDice.remove(dice);
        }

        int score = scoreService.calculateScore(selected);
        int remainingCount = remainingDice.size();
        double risk = calculateRisk(remainingCount);

        // Определяем риск на основе оставшихся кубиков и сложности
        boolean isRisky = remainingCount <= 2 && risk >= 0.65;
        boolean goodMove = selected.size() <= 4 && risk < 0.25 * difficulty;

        Random random = new Random();

        boolean riskForNormalDificulty = random.nextInt(0,2) > risk;

        EndingOfTurn outcome = EndingOfTurn.PASS;

        if (remainingDice.isEmpty() && score > 0 && Game.getInstance().getNpcScore() + score <= Game.getInstance().getScoreToWin()) {
            outcome = EndingOfTurn.SCORED;
        }

        else if  (isRisky) {
            outcome = EndingOfTurn.PASS;
        } else if ( (double)difficulty / 3 < risk || (remainingCount  < 4 && score >= 600  && remainingCount > 1) ||  (remainingCount < 4 && score >= 1000 && remainingCount > 1) || (Game.getInstance().getNpcScore() + score >= Game.getInstance().getScoreToWin())){
            outcome = EndingOfTurn.PASS;
        } else if (goodMove) {
            outcome = EndingOfTurn.SCORED;
        }

        Turn passTurn = new Turn(score, outcome, selected, remainingDice,displayedDice);

        if (depth <= 0 || !shouldContinue(score, remainingCount, risk)) {
            return passTurn;
        }

        Turn continueTurn = simulateContinue(selected, remainingDice, depth, score, risk);
            return passTurn;
    }

    /**
     * Simulates continuing the turn by selecting more dice and adding to the score.
     *
     * @param selected      the list of selected dice for the turn
     * @param remaining     the list of remaining dice
     * @param depth         the depth of the simulation
     * @param score         the current score
     * @param risk          the calculated risk of continuing
     * @return the simulated continued turn
     */
    private Turn simulateContinue(List<Dice> selected, List<Dice> remaining,
                                  int depth, int score, double risk) {
        List<Dice> nextDice = new ArrayList<>(remaining);

        if (nextDice.isEmpty()) {
            nextDice = getDiceDeck().getDeck();
        }

        Turn nextTurn = simulateBestTurn(nextDice, depth - 1);

        return new Turn(
                scoreService.calculateScore(selected),
                nextTurn.getEndingOfTurn(),
                selected,
                nextDice,displayedDice
        );
    }


    /**
     * Selects the optimal turn between two potential turns.
     *
     * @param passTurn    the turn to be passed
     * @param continueTurn the turn to be continued
     * @return the optimal turn
     */
    private Turn selectOptimalTurn(Turn passTurn, Turn continueTurn) {
        double passScore = enhanceScore(passTurn);
        double continueScore = enhanceScore(continueTurn);

        return continueScore > passScore ? continueTurn : passTurn;
    }

    /**
     * Enhances the score of a given turn by factoring in several modifiers
     * like the number of selected dice, risk penalties for busted turns, and
     * the NPC's difficulty level.
     *
     * @param turn The turn whose score is to be enhanced.
     * @return The enhanced score of the turn.
     */
    private double enhanceScore(Turn turn) {
        double baseScore = turn.getTurnScore();
        double sizeBonus = turn.getSelectedDice().size() * 0.1;
        double riskPenalty = turn.getEndingOfTurn() == EndingOfTurn.BUSTED ? 1000 : 0;
        double difficultyMod = 1 + (difficulty * 0.15);

        return (baseScore * (1 + sizeBonus) - riskPenalty) * difficultyMod;
    }

    /**
     * Determines whether the NPC should continue their turn based on the current score,
     * remaining dice, and the calculated risk of continuing. The decision is influenced
     * by the NPC's difficulty level.
     *
     * @param currentScore The current score accumulated by the NPC in the turn.
     * @param diceLeft The number of dice remaining in the current turn.
     * @param risk The calculated risk associated with continuing the turn.
     * @return true if the NPC should continue the turn, false otherwise.
     */
    private boolean shouldContinue(int currentScore, int diceLeft, double risk) {
        double baseChance = 0.4 + (0.25 * difficulty);
        double scoreMod = Math.min(currentScore / 1000.0, 1.0);
        double diceMod = (6 - diceLeft) / 6.0;
        double riskFactor = 1 - (risk * (1.2 - difficulty * 0.4));

        return (baseChance + scoreMod + diceMod) * riskFactor > 0.6;
    }

    /**
     * Calculates the risk associated with continuing the current turn, based on the
     * number of remaining dice. The risk increases as the number of remaining dice decreases.
     * The NPC's difficulty level also influences the calculated risk.
     *
     * @param remainingDiceCount The number of dice remaining in the turn.
     * @return A value representing the risk of continuing the turn, where higher values
     *         indicate greater risk.
     */
    private double calculateRisk(int remainingDiceCount) {
        double baseRisk = 1.0 - (remainingDiceCount / 6.0);
        double difficultyFactor = 1.0 + (difficulty - 1) * 0.2;
        double risk = Math.min(baseRisk * difficultyFactor, 1.0);
        return risk;
    }

    /**
     * Applies the result of the turn by setting the lastTurn variable to the provided turn.
     *
     * @param turn The turn to be applied as the result.
     */
    private void applyTurnResult(Turn turn) {
        this.lastTurn = turn;
    }


    private void resetTurnState() {
        currentTurnScore = 0;
        currentRoll.clear();
        selectedDice.clear();
    }

    // Getters
    public Turn getLastTurn() {
        return lastTurn;
    }

    public List<Dice> getCurrentRoll() {
        return currentRoll;
    }

    public List<Dice> getSelectedDice() {
        return Collections.unmodifiableList(selectedDice);
    }

    public int getCurrentTurnScore() {
        return currentTurnScore;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public Boolean getIntegrity() {
        return integrity;
    }

    public int getMaxBet() {
        return maxBet;
    }

    public int getMinBet() {
        return minBet;
    }

    public String getDescription() {
        return description;
    }

    public String getQuote() {
        return quote;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}
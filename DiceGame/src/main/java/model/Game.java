package model;

import db.GameDatabase;
import exceptions.SomeGameFieldsMissing;
import model.observers.GameObserver;
import model.records.dice.Dice;
import model.records.npc.HumanPlayer;
import model.records.npc.NPC;
import model.records.npc.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.PlayerService;
import view.applications.MainApplication;

import java.io.Serializable;
import java.util.List;

/**
 * The main game class that manages the core game logic, player and NPC interactions,
 * turn management, and scoring system.
 * <p>
 * This class follows the Singleton pattern to ensure only one instance exists during runtime.
 * It maintains game state including current turn, player/NPC points, and game bet.
 * </p>
 */
public class Game implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final Logger logger = LoggerFactory.getLogger(Game.class);

    private List<Dice> rolledDice;
    private static Game instance;
    private Player player;
    private NPC npc;
    private int currentTurn;
    private int gameBet = 0;

    private int turnScore = 0;

    private int npcScore = 0;
    private int playerScore = 0;

    private int scoreToWin = 5000;

    private GameObserver gameObserver;

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private Game() {
        logger.debug("Game instance created");
    }

    /**
     * Returns the singleton instance of the Game class.
     *
     * @return The single instance of Game
     */
    public static Game getInstance() {
        if (instance == null) {
            logger.trace("Creating new Game instance (first time)");
            instance = new Game();
        }
        logger.debug("Returning Game instance");
        return instance;
    }

    /**
     * Initializes and starts the game after validating required fields.
     *
     * @throws SomeGameFieldsMissing if any required game fields (player, NPC, or bet) are missing
     */
    private void startGame() throws SomeGameFieldsMissing {
        logger.info("Attempting to start the game");

        if (player == null || npc == null || gameBet == 0 || npc.getMaxBet() == 0) {
            String errorMsg = "Cannot start game - missing required fields: " +
                    (player == null ? "player, " : "") +
                    (npc == null ? "npc, " : "") +
                    (gameBet == 0 ? "gameBet, " : "") +
                    (npc != null && npc.getMaxBet() == 0 ? "npc.maxBet" : "");
            logger.error(errorMsg);
            throw new SomeGameFieldsMissing("Some game fields are missing" + errorMsg);
        }

        currentTurn = 0;
        logger.info("Game started successfully. First turn: {}", currentTurn == 0 ? "Player" : "NPC");
        if (gameObserver != null) {
            throwDice();
        }
    }

    /**
     * Configures the game with the selected NPC and initial bet.
     *
     * @param npc The NPC opponent to play against
     * @param gameBet The bet to be placed in the game
     * @throws SomeGameFieldsMissing if setup validation fails
     */
    public void setUpGame(NPC npc, int gameBet) throws SomeGameFieldsMissing {
        player = PlayerService.getInstance().getPlayer();
        logger.info("Setting up game with NPC: {}", npc != null ? npc.getClass().getSimpleName() : "null");
        this.npc = npc;
        this.gameBet = gameBet;
        this.npcScore = 0;
        this.playerScore = 0;

        logger.info("Game setup complete with bet: {}", gameBet);
    }

    /**
     * Rolls the dice for the current turn.
     * If the current turn is the player, it rolls the player's dice.
     * Else, it rolls the NPC's dice.
     */
    public void throwDice() {
        if (currentTurn == 0) {
            player.getDiceDeck().roll();
            rolledDice = player.getDiceDeck().getDeck();
            gameObserver.playerRollDice();
        } else {
            rolledDice = npc.getDiceDeck().getDeck();
            npc.getDiceDeck().roll();
            npc.rollDice(rolledDice);
            gameObserver.npcRollDice();
        }
    }

    /**
     * Ends the current turn and switches to the other player's turn.
     * Also updates the score by adding points for the current turn.
     */
    public void endTurn() {
        logger.debug("Ending turn. Current turn was: {}", currentTurn == 0 ? "Player" : "NPC");

        int oldTurn = currentTurn;
        currentTurn = (currentTurn == 0) ? 1 : 0;

        logger.info("Turn changed from {} to {}",
                oldTurn == 0 ? "Player" : "NPC",
                currentTurn == 0 ? "Player" : "NPC");

        checkIfAnyOneWin();
    }

    /**
     * Gets the current player instance.
     *
     * @return The Player object representing the human player
     */
    public Player getPlayer() {
        logger.trace("Accessing player instance");
        return player;
    }

    /**
     * Sets the player instance for the game.
     *
     * @param player The Player object to set
     */
    public void setPlayer(Player player) {
        logger.info("Setting player instance: {}", player != null ? player.getClass().getSimpleName() : "null");
        this.player = player;
    }

    /**
     * Gets the current NPC opponent instance.
     *
     * @return The NPC object representing the computer opponent
     */
    public NPC getNpc() {
        logger.trace("Accessing NPC instance");
        return npc;
    }

    /**
     * Sets the NPC opponent for the game.
     *
     * @param npc The NPC object to set
     */
    public void setNpc(NPC npc) {
        logger.info("Setting NPC instance: {}", npc != null ? npc.getClass().getSimpleName() : "null");
        this.npc = npc;
    }

    /**
     * Gets the current turn indicator.
     *
     * @return 0 for player's turn, 1 for NPC's turn
     */
    public int getCurrentTurn() {
        logger.trace("Getting current turn: {}", currentTurn == 0 ? "Player" : "NPC");
        return currentTurn;
    }

    /**
     * Sets the current turn indicator.
     *
     * @param currentTurn The value to set the current turn (0 for Player, 1 for NPC)
     */
    public void setCurrentTurn(int currentTurn) {
        logger.info("Setting current turn: {}", currentTurn == 0 ? "Player" : "NPC");
        this.currentTurn = currentTurn;
    }

    /**
     * Gets the current game bet.
     *
     * @return The bet placed for the game
     */
    public int getGameBet() {
        return gameBet;
    }

    /**
     * Gets the current score for the turn.
     *
     * @return The score accumulated in the current turn
     */
    public int getTurnScore() {
        return turnScore;
    }

    /**
     * Sets the score for the current turn.
     *
     * @param turnScore The score to set for the current turn
     */
    public void setTurnScore(int turnScore) {
        this.turnScore = turnScore;
    }

    /**
     * Gets the GameObserver instance.
     *
     * @return The GameObserver instance that monitors game events
     */
    public GameObserver getGameObserver() {
        return gameObserver;
    }

    /**
     * Sets the GameObserver instance and starts the game.
     *
     * @param gameObserver The observer to attach to the game
     * @throws SomeGameFieldsMissing if required fields are missing during the game setup
     */
    public void setGameObserver(GameObserver gameObserver) throws SomeGameFieldsMissing {
        this.gameObserver = gameObserver;
        startGame();
    }

    /**
     * Gets the list of rolled dice for the current turn.
     *
     * @return A list of Dice objects representing the rolled dice
     */
    public List<Dice> getRolledDice() {
        return rolledDice;
    }

    /**
     * Sets the list of rolled dice for the current turn.
     *
     * @param rolledDice A list of Dice objects to set as the rolled dice
     */
    public void setRolledDice(List<Dice> rolledDice) {
        this.rolledDice = rolledDice;
    }

    /**
     * Gets the current score of the NPC.
     *
     * @return The score of the NPC
     */
    public int getNpcScore() {
        return npcScore;
    }

    /**
     * Gets the score required to win the game.
     *
     * @return The score required to win the game
     */
    public int getScoreToWin() {
        return scoreToWin;
    }

    /**
     * Sets the score required to win the game.
     *
     * @param scoreToWin The score to set as the winning score
     */
    public void setScoreToWin(int scoreToWin) {
        this.scoreToWin = scoreToWin;
    }

    /**
     * Sets the current score of the NPC.
     *
     * @param npcScore The score to set for the NPC
     */
    public void setNpcScore(int npcScore) {
        this.npcScore = npcScore;
    }

    /**
     * Checks if either the player or the NPC has won the game.
     */
    public void checkIfAnyOneWin() {
        if (npcScore >= scoreToWin) {
            player.setBalance(player.getBalance() - gameBet);
            gameObserver.displayWin(npc.getName(), gameBet);
        } else if (playerScore >= scoreToWin) {
            player.setBalance(player.getBalance() + gameBet);
            gameObserver.displayWin(player.getName(), gameBet);
        } else {
            if (gameObserver != null) {
                throwDice();
            }
        }
        if (player != null) {
            GameDatabase.getInstance().save((HumanPlayer) player);
        }
    }

    /**
     * Gets the current score of the player.
     *
     * @return The score of the player
     */
    public int getPlayerScore() {
        return playerScore;
    }

    /**
     * Sets the current score of the player.
     *
     * @param playerScore The score to set for the player
     */
    public void setPlayerScore(int playerScore) {
        this.playerScore = playerScore;
    }

    /**
     * Cancels the game by resetting the observer and NPC.
     */
    public void cancelGame() {
        gameObserver = null;
        npc = null;
    }

    /**
     * Displays a lucky message.
     */
    public void showLuckyMesage() {
        // Message logic to be added
    }

    /**
     * Displays a royal dice message.
     */
    public void showRoyalDiceMessage() {
        // Message logic to be added
    }

    /**
     * Resets the game instance to null, effectively ending the game.
     */
    public void setGameNull() {
        instance = null;
    }
}
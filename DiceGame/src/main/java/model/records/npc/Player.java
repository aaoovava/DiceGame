package model.records.npc;

import model.Game;
import model.records.dice.Dice;
import model.records.dice.DiceDeck;

import java.io.Serializable;

/**
 * Abstract class representing a player in the game, storing player-specific details like
 * name, dice deck, balance, and current bet, and providing functionality to roll dice.
 *
 * <p>This class serves as a base class for different types of players (e.g., human or NPC).</p>
 */
public abstract class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private DiceDeck diceDeck;
    private int balance;
    private int currentBet;

    private Game game = Game.getInstance();

    /**
     * Constructs a new Player instance with the given name, dice deck, balance, and current bet.
     *
     * @param name the name of the player
     * @param diceDeck the deck of dice owned by the player
     * @param balance the balance of the player
     * @param curentBet the current bet of the player
     */
    public Player(String name, DiceDeck diceDeck, int balance, int curentBet) {
        this.name = name;
        this.diceDeck = diceDeck;
        this.balance = balance;
        this.currentBet = curentBet;
    }

    /**
     * Rolls all the dice in the player's dice deck and updates the game state with the rolled dice.
     */
    public void rollDice() {
        diceDeck.roll();
        game.setRolledDice(diceDeck.getDeck());
    }

    /**
     * Gets the name of the player.
     *
     * @return the name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the player.
     *
     * @param name the new name of the player
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the dice deck of the player.
     *
     * @return the dice deck of the player
     */
    public DiceDeck getDiceDeck() {
        return diceDeck;
    }

    /**
     * Sets the dice deck for the player.
     *
     * @param diceDeck the new dice deck for the player
     */
    public void setDiceDeck(DiceDeck diceDeck) {
        this.diceDeck = diceDeck;
    }

    /**
     * Gets the current balance of the player.
     *
     * @return the current balance of the player
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Sets the balance for the player.
     *
     * @param balance the new balance of the player
     */
    public void setBalance(int balance) {
        this.balance = balance;
    }

    /**
     * Gets the current bet of the player.
     *
     * @return the current bet of the player
     */
    public int getCurrentBet() {
        return currentBet;
    }

    /**
     * Sets the current bet for the player.
     *
     * @param currentBet the new current bet of the player
     */
    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }
}
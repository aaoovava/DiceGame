package model.records.npc;

import model.records.dice.DiceDeck;

import java.io.Serializable;

/**
 * Represents a human player in the game, storing player-specific details like
 * name, dice deck, balance, and current bet, and providing functionality to roll dice.
 */
public class HumanPlayer extends Player implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new HumanPlayer instance with the given name, dice deck, balance, and current bet.
     *
     * @param name the name of the player
     * @param diceDeck the deck of dice owned by the player
     * @param balance the balance of the player
     * @param curentBet the current bet of the player
     */
    public HumanPlayer(String name, DiceDeck diceDeck, int balance, int curentBet) {
        super(name, diceDeck, balance, curentBet);
    }


    public Boolean tryToDetectCheat() {
        return null;
    }
}

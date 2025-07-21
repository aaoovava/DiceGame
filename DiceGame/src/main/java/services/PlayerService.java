package services;

import db.GameDatabase;
import model.records.dice.*;
import model.records.npc.HumanPlayer;
import model.records.npc.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton service class responsible for managing the player's data and dice deck.
 *
 * <p>This class handles player-related functionality, including retrieving and updating player data
 * from the database, managing the player's dice deck, and maintaining a singleton instance of the player
 * for game logic purposes.</p>
 */
public class PlayerService {
    private static PlayerService instance;
    private static final GameDatabase db = GameDatabase.getInstance();
    private HumanPlayer player;
    private List<Dice> deck = new ArrayList<>();

    /**
     * Private constructor to initialize the player and their dice deck.
     * The dice deck is pre-populated with several types of dice (Regular, Lucky, Cursed, Royal).
     * If no player data is found in the database, a new player is created and saved.
     */
    private PlayerService() {
        for (int i = 0; i < 6; i++) {
            deck.add(new RegularDice(1));
        }

        player = db.load();
        if (player == null) {
            player = new HumanPlayer("player", new DiceDeck(deck), 50, 600);
            db.save(player);
        }
    }

    /**
     * Retrieves the singleton instance of the PlayerService.
     *
     * @return the singleton instance of PlayerService
     */
    public static PlayerService getInstance() {
        if (instance == null) {
            instance = new PlayerService();
        }
        return instance;
    }

    /**
     * Sets the current player to the provided player.
     *
     * @param player the new {@link HumanPlayer} object to set as the current player
     */
    public void setPlayer(HumanPlayer player) {
        this.player = player;
    }

    /**
     * Gets the current player.
     *
     * @return the current {@link HumanPlayer} object
     */
    public HumanPlayer getPlayer() {
        return player;
    }

    /**
     * Gets the name of the current player.
     *
     * @return the name of the current player
     */
    public String getName() {
        return player.getName();
    }

    /**
     * Sets the name of the current player and updates the database with the new name.
     *
     * @param name the new name of the player
     */
    public void setName(String name) {
        player.setName(name);
        db.save(player);
    }

    /**
     * Resets the singleton instance of PlayerService to null.
     * This can be used to release the singleton instance, making it eligible for garbage collection.
     */
    public void setPlayerServiceNull() {
        instance = null;
    }

    /**
     * Gets the balance of the current player.
     *
     * @return the balance of the current player
     */
    public int getBalance() {
        return player.getBalance();
    }
}
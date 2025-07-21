package multiplayer;

import model.observers.GameObserver;
import model.records.Turn;
import model.records.dice.Dice;
import model.records.npc.Player;

import java.util.List;
import java.util.function.Consumer;

/**
 * This interface serves as an adapter for a player, providing
 * communication between the client and the game server. It implements the
 * PlayerAdapter interface for handling player-specific actions such as
 * taking turns and updating scores.
 */
public interface PlayerAdapter <T extends Player>{
    String getName();
    int getScore();
    void updateScore(int score);
    void takeTurn(List<Dice> availableDice, Consumer<Turn> onTurnComplete);
    void setGameObserver(GameObserver observer);
}

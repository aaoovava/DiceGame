package multiplayer;

import model.observers.GameObserver;
import model.records.Turn;
import model.records.dice.Dice;
import model.records.npc.HumanPlayer;
import model.records.npc.Player;

import java.util.List;
import java.util.function.Consumer;

/**
 * This class serves as an adapter for a remote human player, providing
 * communication between the client and the game server. It implements the
 * PlayerAdapter interface for handling player-specific actions such as
 * taking turns and updating scores.
 */
public class RemotePlayerAdapter implements PlayerAdapter<HumanPlayer> {
    private final HumanPlayer remotePlayer;
    private final GameClient gameClient;
    private GameObserver observer;
    private int score;

    /**
     * Constructs a RemotePlayerAdapter for the given player and server address.
     *
     * @param player The human player to be adapted.
     * @param serverAddress The server address to connect to.
     */
    public RemotePlayerAdapter(HumanPlayer player, String serverAddress) {
        this.remotePlayer = player;
        this.gameClient = new GameClient(serverAddress, false);
        this.gameClient.setAdapter(this);
    }

    /**
     * Returns the name of the remote player.
     *
     * @return The name of the remote player.
     */
    @Override
    public String getName() {
        return remotePlayer.getName();
    }

    /**
     * Returns the current score of the remote player.
     *
     * @return The score of the remote player.
     */
    @Override
    public int getScore() {
        return score;
    }

    /**
     * Updates the score of the remote player and sends the updated score
     * to the game server.
     *
     * @param score The new score to be set for the player.
     */
    @Override
    public void updateScore(int score) {
        this.score = score;
        gameClient.sendScoreUpdate(score);
    }

    /**
     * Requests a turn for the remote player, providing the available dice
     * and a callback for when the turn is complete.
     *
     * @param availableDice A list of available dice for the turn.
     * @param onTurnComplete A callback to be invoked when the turn is complete.
     */
    @Override
    public void takeTurn(List<Dice> availableDice, Consumer<Turn> onTurnComplete) {
        gameClient.requestTurn(availableDice, onTurnComplete);
    }

    /**
     * Sets the observer for the game, which listens for game events.
     *
     * @param observer The game observer to be set.
     */
    @Override
    public void setGameObserver(GameObserver observer) {
        this.observer = observer;
    }

    /**
     * This method is called by the GameClient when the remote player takes
     * a turn. It notifies the observer to update the game state accordingly.
     *
     * @param turn The turn that the remote player has completed.
     */
    public void handleRemoteTurn(Turn turn) {
        if (observer != null) {
            observer.playerRollDice();
        }
    }
}
package multiplayer;

import model.records.Turn;
import model.records.dice.Dice;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;

/**
 * The GameClient class is responsible for managing the connection to the game server.
 * It facilitates communication between the client and the server, handling the sending
 * and receiving of game-related data, such as turn requests and score updates.
 */
public class GameClient {
    private final String serverAddress;
    private final int serverPort = 1008;
    protected Socket socket;
    protected ObjectOutputStream outputStream;
    protected ObjectInputStream inputStream;
    private RemotePlayerAdapter adapter;
    protected boolean connected = false;

    /**
     * Constructs a GameClient instance for connecting to a server.
     *
     * @param serverAddress The address of the game server.
     * @param autoConnect If true, automatically attempts to connect to the server.
     */
    public GameClient(String serverAddress, boolean autoConnect) {
        this.serverAddress = serverAddress;
        if (autoConnect) {
            connectToServer();
        }
    }

    /**
     * Sets the adapter that will handle player actions and game events.
     *
     * @param adapter The adapter for handling remote player actions.
     */
    public void setAdapter(RemotePlayerAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Establishes a connection to the game server.
     * It sets up the necessary input and output streams for communication.
     */
    protected void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            setupStreams(socket.getOutputStream(), socket.getInputStream());
            connected = true;
            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    /**
     * Initializes the input and output streams for communication with the server.
     *
     * @param output The output stream to send data to the server.
     * @param input The input stream to receive data from the server.
     * @throws IOException If an error occurs while setting up the streams.
     */
    protected void setupStreams(OutputStream output, InputStream input) throws IOException {
        this.outputStream = new ObjectOutputStream(output);
        this.inputStream = new ObjectInputStream(input);
    }

    // --- 💡 NEW setters ---

    /**
     * Sets the socket for the client connection.
     *
     * @param socket The socket to be used for the connection.
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * Sets the output stream used for sending data to the server.
     *
     * @param outputStream The output stream to be set.
     */
    public void setOutputStream(ObjectOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Sets the input stream used for receiving data from the server.
     *
     * @param inputStream The input stream to be set.
     */
    public void setInputStream(ObjectInputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Sets the connection status of the client.
     *
     * @param connected The connection status to be set.
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    // ----------------------

    /**
     * Sends a request to the server to initiate a player's turn, passing the available dice
     * and a callback to handle the turn once it's completed.
     *
     * @param availableDice A list of dice available for the player's turn.
     * @param callback A callback function to be called when the turn is complete.
     */
    public void requestTurn(List<Dice> availableDice, Consumer<Turn> callback) {
        if (!connected) return;

        try {
            outputStream.writeObject(new TurnRequest(availableDice));
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Error sending turn request: " + e.getMessage());
        }
    }

    /**
     * Sends an updated score to the server for the current player.
     *
     * @param score The updated score to be sent.
     */
    public void sendScoreUpdate(int score) {
        if (!connected) return;

        try {
            outputStream.writeObject(new ScoreUpdate(score));
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Error sending score update: " + e.getMessage());
        }
    }

    /**
     * Listens for incoming messages from the server, such as updates on turns or scores,
     * and processes them accordingly.
     */
    private void listenForMessages() {
        while (connected) {
            try {
                Object received = inputStream.readObject();

                if (received instanceof Turn) {
                    Turn turn = (Turn) received;
                    if (adapter != null) {
                        adapter.handleRemoteTurn(turn);
                    }
                } else if (received instanceof ScoreUpdate) {
                    // Handle opponent score updates if needed
                }

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Connection lost: " + e.getMessage());
                connected = false;
                closeConnection();
            }
        }
    }

    /**
     * Closes the connection by closing the input and output streams and the socket.
     */
    private void closeConnection() {
        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Represents a request for the player's turn, containing the available dice.
     */
    private static class TurnRequest implements Serializable {
        final List<Dice> availableDice;

        TurnRequest(List<Dice> availableDice) {
            this.availableDice = availableDice;
        }
    }

    /**
     * Represents a score update, containing the updated score of the player.
     */
    private static class ScoreUpdate implements Serializable {
        final int score;

        ScoreUpdate(int score) {
            this.score = score;
        }
    }

    /**
     * Returns the current RemotePlayerAdapter associated with this GameClient.
     *
     * @return The RemotePlayerAdapter for this client.
     */
    public RemotePlayerAdapter getAdapter() {
        return adapter;
    }
}
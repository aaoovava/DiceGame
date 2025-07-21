package db;

import model.records.dice.RegularDice;
import model.records.npc.HumanPlayer;
import model.records.dice.DiceDeck;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;

/**
 * Singleton class that handles all database operations for the game.
 *
 * <p>This class provides methods to save and load player data (specifically, a {@link HumanPlayer})
 * to and from an SQLite database. The player data is serialized into a Base64-encoded string for storage.</p>
 */
public class GameDatabase {
    private static final String URL = "jdbc:sqlite:game_data.db";
    private static final GameDatabase INSTANCE = new GameDatabase();

    /**
     * Private constructor for initializing the database.
     * Creates the player_data table if it does not exist.
     */
    private GameDatabase() {
        initDatabase();
    }

    /**
     * Retrieves the singleton instance of the GameDatabase.
     *
     * @return the singleton instance of the GameDatabase
     */
    public static GameDatabase getInstance() {
        return INSTANCE;
    }

    /**
     * Establishes a connection to the SQLite database.
     *
     * @return a {@link Connection} object to the database
     * @throws SQLException if a database access error occurs
     */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * Initializes the game database, creating the necessary table if it does not already exist.
     * Specifically, it creates the player_data table for storing serialized player data.
     */
    private void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS player_data ("
                + "id INTEGER PRIMARY KEY CHECK(id = 1), "
                + "player_data TEXT)"; // Changed to TEXT for Base64 encoding

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Saves the provided {@link HumanPlayer} object to the database.
     * If player data already exists, it will be updated.
     *
     * @param player the player object to save
     */
    public void save(HumanPlayer player) {
        String sql = "INSERT INTO player_data (id, player_data) VALUES (1, ?) "
                + "ON CONFLICT(id) DO UPDATE SET player_data = excluded.player_data";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String serialized = serializePlayer(player);
            pstmt.setString(1, serialized);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save player", e);
        }
    }

    /**
     * Loads the player data from the database.
     *
     * @return the {@link HumanPlayer} object if found, or null if no data exists
     */
    public HumanPlayer load() {
        String sql = "SELECT player_data FROM player_data WHERE id = 1";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String data = rs.getString("player_data");
                return deserializePlayer(data);
            }
        } catch (SQLException e) {
            // Handle the exception silently (i.e., return null if no player is found)
        }
        return null;
    }

    /**
     * Serializes a {@link HumanPlayer} object into a Base64-encoded string.
     *
     * @param player the player object to serialize
     * @return a Base64-encoded string representing the serialized player data
     */
    private String serializePlayer(HumanPlayer player) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            PlayerData data = new PlayerData(
                    player.getName(),
                    player.getDiceDeck(),
                    player.getBalance(),
                    player.getCurrentBet()
            );
            oos.writeObject(data);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            System.err.println("Error serializing player: " + e.getMessage());
            throw new RuntimeException("Serialization failed", e);
        }
    }

    /**
     * Deserializes the Base64-encoded player data into a {@link HumanPlayer} object.
     *
     * @param data the Base64-encoded string representing the player data
     * @return a {@link HumanPlayer} object or null if deserialization fails
     */
    private HumanPlayer deserializePlayer(String data) {
        if (data == null || data.isEmpty()) return null;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(
                Base64.getDecoder().decode(data));
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            PlayerData pd = (PlayerData) ois.readObject();
            return new HumanPlayer(pd.name, pd.diceDeck, pd.balance, pd.currentBet);
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * A helper class used for serializing the player data.
     * This class stores the necessary fields to recreate a {@link HumanPlayer}.
     */
    private static class PlayerData implements Serializable {
        private static final long serialVersionUID = 2L; // Incremented version
        final String name;
        final DiceDeck diceDeck;
        final int balance;
        final int currentBet;

        PlayerData(String name, DiceDeck diceDeck, int balance, int currentBet) {
            this.name = name;
            this.diceDeck = diceDeck;
            this.balance = balance;
            this.currentBet = currentBet;
        }
    }
}
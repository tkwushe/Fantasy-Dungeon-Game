package com.game.database;

import java.sql.*;
import java.util.Date;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;

public class GameDatabaseService {
    private static final Logger LOGGER = Logger.getLogger(GameDatabaseService.class.getName());
    
    private static final String CREATE_SAVES_TABLE = 
        "CREATE TABLE IF NOT EXISTS game_saves (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "save_name TEXT NOT NULL," +
        "player_data BLOB NOT NULL," +
        "save_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
        ")";

    public GameDatabaseService() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_SAVES_TABLE);
            LOGGER.info("Database initialized successfully");
        } catch (SQLException e) {
            LOGGER.severe("Failed to initialize database: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public void saveGameState(GameState state) {
        if (state == null || state.getPlayer() == null) {
            throw new IllegalArgumentException("Invalid game state");
        }

        String saveName = generateSaveName(state.getPlayer().getName());
        byte[] serializedState = serializeGameState(state);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO game_saves (save_name, player_data) VALUES (?, ?)")) {
            
            pstmt.setString(1, saveName);
            pstmt.setBytes(2, serializedState);
            
            int result = pstmt.executeUpdate();
            if (result != 1) {
                throw new SQLException("Failed to save game state");
            }
            
            LOGGER.info("Game saved successfully: " + saveName);
        } catch (SQLException e) {
            LOGGER.severe("Error saving game state: " + e.getMessage());
            throw new RuntimeException("Failed to save game state", e);
        }
    }

    public GameState loadGameState(String saveName) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT player_data FROM game_saves WHERE save_name = ?")) {
            
            pstmt.setString(1, saveName);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    byte[] stateBytes = rs.getBytes("player_data");
                    return deserializeGameState(stateBytes);
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading game state: " + e.getMessage());
            throw new RuntimeException("Failed to load game state", e);
        }
        return null;
    }

    public List<String> getAvailableSaves() {
        List<String> saves = new ArrayList<>();
        String query = "SELECT save_name FROM game_saves ORDER BY save_date DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                saves.add(rs.getString("save_name"));
            }
        } catch (SQLException e) {
            LOGGER.severe("Error retrieving save files: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve save files", e);
        }
        return saves;
    }

    private String generateSaveName(String playerName) {
        return playerName + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    }

    private byte[] serializeGameState(GameState state) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(state);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize game state", e);
        }
    }

    private GameState deserializeGameState(byte[] stateBytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(stateBytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (GameState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize game state", e);
        }
    }
} 
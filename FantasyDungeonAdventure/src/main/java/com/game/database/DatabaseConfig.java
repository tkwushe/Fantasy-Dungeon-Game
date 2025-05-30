package com.game.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Manages database configuration and connections.
 */
public class DatabaseConfig {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static final String DB_URL = "jdbc:sqlite:game_saves.db";
    
    private DatabaseConfig() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets a database connection.
     * @return A Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Ensure SQLite JDBC driver is loaded
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(DB_URL);
        } catch (ClassNotFoundException e) {
            String errorMsg = String.format("SQLite JDBC driver not found. Please ensure SQLite dependency is properly included. Error: %s", e.getMessage());
            LOGGER.severe(errorMsg);
            throw new SQLException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = String.format("Failed to connect to database at %s. Error: %s", DB_URL, e.getMessage());
            LOGGER.severe(errorMsg);
            throw new SQLException(errorMsg, e);
        }
    }
}

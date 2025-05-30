package com.game;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.game.gui.GameWindow;
import com.game.util.LogManager;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

/**
 * Main entry point for the game.
 * Handles initialization of logging, UI, and game systems.
 */
public class GameLauncher {
    private static final Logger LOGGER = Logger.getLogger(GameLauncher.class.getName());

    public static void main(String[] args) {
        try {
            // Initialize logging system
            LogManager.initialize();
            LOGGER.info("Starting Fantasy Dungeon Adventure...");

            // Set system look and feel
            setSystemLookAndFeel();

            // Initialize and show game window on EDT
            SwingUtilities.invokeLater(() -> {
                try {
                    GameWindow gameWindow = GameWindow.getInstance();
                    gameWindow.initializeGame();
                    LOGGER.info("Game window initialized successfully.");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to initialize game window", e);
                    showErrorAndExit("Failed to initialize game window: " + e.getMessage());
                }
            });

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize logging system", e);
            showErrorAndExit("Failed to initialize logging system: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during game startup", e);
            showErrorAndExit("Unexpected error during game startup: " + e.getMessage());
        }
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            LOGGER.info("System look and feel set successfully.");
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | 
                 InstantiationException | IllegalAccessException e) {
            // Non-critical error, log warning but continue with default look and feel
            LOGGER.log(Level.WARNING, "Could not set system look and feel", e);
        }
    }

    private static void showErrorAndExit(String message) {
        javax.swing.JOptionPane.showMessageDialog(null,
            message + "\nPlease check the logs for more details.",
            "Error Starting Game",
            javax.swing.JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
} 

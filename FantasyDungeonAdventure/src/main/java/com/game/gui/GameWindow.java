package com.game.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import com.game.database.GameDatabaseService;
import com.game.database.GameState;
import com.game.engine.GameEngine;
import com.game.event.*;
import com.game.event.handlers.UIEventHandler;
import com.game.puzzle.Puzzle;
import com.game.player.Player;
import com.game.util.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

public class GameWindow extends JFrame {
    private JTextArea outputArea;
    private JTextField commandInput;
    private MapPanel mapPanel;
    private InventoryPanel inventoryPanel;
    private static GameWindow instance;
    private static final Object LOCK = new Object();
    private static boolean isInitialized = false;
    private static final Logger LOGGER = LogManager.getLogger(GameWindow.class.getName());

    /**
     * Returns the singleton instance of the GameWindow class.
     * This method ensures that only one instance of GameWindow is created and used throughout the application.
     * It uses double-checked locking for thread safety and performance optimization.
     *
     * @return The singleton instance of GameWindow.
     */
    public static synchronized GameWindow getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null && !isInitialized) {
                    instance = new GameWindow();
                    isInitialized = true;
                }
            }
        }
        return instance;
    }

    private GameWindow() {
        super(" Fantasy Dungeon Adventure");
        
        if (isInitialized) {
            throw new IllegalStateException("GameWindow already initialized!");
        }
        
        // Setup window properties
        setSize(1024, 768);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize components
        initializeComponents();
        
        // Initialize event handler
        UIEventHandler uiEventHandler = new UIEventHandler(this);
        GameEventDispatcher.getInstance().registerHandler(uiEventHandler);

        // Center window
        setLocationRelativeTo(null);
    }

    public void initializeGame() {
        if (!isVisible()) {
            // Clear the output area first
            outputArea.setText("");
            
            // Initialize the game engine first
            GameEngine gameEngine = GameEngine.getInstance();
            
            // Make window visible
            setVisible(true);
            
            // Request focus for command input
            commandInput.requestFocusInWindow();
            
            // Start the game after window is visible and events are registered
            SwingUtilities.invokeLater(() -> {
                gameEngine.startGame();
                // Only update map and inventory after player is initialized
                if (gameEngine.getPlayer() != null) {
                    updateMap();
                    updateInventory();
                }
            });
        }
    }

    private void initializeComponents() {
        // Game output
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        outputArea.setBackground(Color.BLACK);
        outputArea.setForeground(Color.GREEN);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Command input with improved handling
        commandInput = new JTextField();
        commandInput.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        commandInput.setBackground(Color.DARK_GRAY);
        commandInput.setForeground(Color.WHITE);
        commandInput.addActionListener(e -> {
            String command = commandInput.getText().trim();
            if (!command.isEmpty()) {
                processCommand(command);
                commandInput.setText("");
                // Request focus back to input field
                commandInput.requestFocusInWindow();
            }
        });

        // Add key listener for command history
        commandInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    commandInput.setText("");
                }
            }
        });

        // Ensure input field always has focus when window is activated
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                commandInput.requestFocusInWindow();
            }
        });

        // Side panels
        mapPanel = new MapPanel();
        inventoryPanel = new InventoryPanel();

        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(commandInput, BorderLayout.SOUTH);

        // Add components to frame
        add(mapPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(inventoryPanel, BorderLayout.EAST);

        // Add menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        
        JMenuItem saveMenuItem = new JMenuItem("Save Game");
        saveMenuItem.addActionListener(e -> handleSaveGameRequest(null));
        
        JMenuItem loadMenuItem = new JMenuItem("Load Game");
        loadMenuItem.addActionListener(e -> handleLoadGameRequest());
        
        gameMenu.add(saveMenuItem);
        gameMenu.add(loadMenuItem);
        menuBar.add(gameMenu);
        
        setJMenuBar(menuBar);
    }

    private void processCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }
        
        // Display command in output area
        SwingUtilities.invokeLater(() -> {
            outputArea.append("> " + command + "\n");
            // Ensure the latest output is visible
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });

        // Process command in game engine
        try {
            GameEngine.getInstance().processCommand(command);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing command: " + command, e);
            SwingUtilities.invokeLater(() -> {
                outputArea.append("Error processing command. Please try again.\n");
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            });
        }
    }

    public void updateMap() {
        if (mapPanel != null) {
            Player player = GameEngine.getInstance().getPlayer();
            if (player != null && player.getLocation() != null) {
                String roomId = player.getLocation().getRoomId();
                mapPanel.revealRoom(roomId, player.getLocation().hasTreasure());
                mapPanel.updatePlayerPosition(roomId);
                mapPanel.revalidate();
                mapPanel.repaint();
            }
        }
    }

    public void updateInventory() {
        SwingUtilities.invokeLater(() -> {
            Player player = GameEngine.getInstance().getPlayer();
            if (player != null && inventoryPanel != null) {
                inventoryPanel.updateInventory(player.getInventory());
                inventoryPanel.updateStatus(player);
            }
        });
    }

    @Override
    public void dispose() {
        if (inventoryPanel != null) {
            inventoryPanel.cleanup();
        }
        super.dispose();
    }

    // Add this method to clear the output area
    public void clearOutput() {
        SwingUtilities.invokeLater(() -> {
            outputArea.setText("");
            if (mapPanel != null) {
                mapPanel.resetMap();
            }
            if (inventoryPanel != null) {
                inventoryPanel.reset();
            }
            commandInput.requestFocusInWindow();
            revalidate();
            repaint();
        });
    }

    private void showLoadGameDialog() {
        // Create instance of GameDatabaseService
        GameDatabaseService dbService = new GameDatabaseService();
        
        // Get list of available saves
        List<String> saveFiles = dbService.getAvailableSaves();
        
        if (saveFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No saved games found!",
                "Load Game", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Format save names for display
        String[] displayNames = saveFiles.stream()
            .map(this::formatSaveName)
            .toArray(String[]::new);

        // Show selection dialog
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "Choose a save file to load:",
            "Load Game",
            JOptionPane.QUESTION_MESSAGE,
            null,
            displayNames,
            displayNames[0]
        );

        if (selected != null) {
            // Convert display name back to save name
            String saveName = getSaveNameFromDisplay(selected);
            clearOutput();
            GameEngine.getInstance().loadGame(saveName);
        }
    }

    private String formatSaveName(String saveName) {
        // Convert "tk_20241215_153022" to "tk - December 15, 2024 15:30:22"
        String[] parts = saveName.split("_");
        if (parts.length == 3) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyyMMdd_HHmmss");
                SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss");
                Date saveDate = parser.parse(parts[1] + "_" + parts[2]);
                return parts[0] + " - " + formatter.format(saveDate);
            } catch (ParseException e) {
                return saveName;
            }
        }
        return saveName;
    }

    private String getSaveNameFromDisplay(String displayName) {
        // Convert "John - March 15, 2024 15:30:22" back to "John_20240315_153022"
        try {
            String[] parts = displayName.split(" - ");
            String playerName = parts[0];
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss");
            SimpleDateFormat saveFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date date = displayFormat.parse(parts[1]);
            return playerName + "_" + saveFormat.format(date);
        } catch (ParseException e) {
            LOGGER.warning("Error parsing save name: " + e.getMessage());
            return displayName;
        }
    }

    private void showSaveGameDialog() {
        try {
            Player player = GameEngine.getInstance().getPlayer();
            if (player == null) {
                JOptionPane.showMessageDialog(this,
                    "Cannot save: No active game",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            String playerName = JOptionPane.showInputDialog(this,
                "Enter a name for your save:",
                "Save Game",
                JOptionPane.QUESTION_MESSAGE);
            
            if (playerName != null && !playerName.trim().isEmpty()) {
                player.setName(playerName.trim());
                
                GameState currentState = new GameState();
                currentState.setPlayer(player);
                currentState.setLevels(GameEngine.getInstance().getLevels());
                currentState.setCurrentLevelIndex(GameEngine.getInstance().getCurrentLevelIndex());
                
                GameDatabaseService dbService = new GameDatabaseService();
                dbService.saveGameState(currentState);
                
                JOptionPane.showMessageDialog(this,
                    "Game saved successfully!",
                    "Save Game",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to save game: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Failed to save game: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles save game requests from the game engine
     * @param gameState The current game state to save
     */
    public void handleSaveGameRequest(GameState gameState) {
        showSaveGameDialog();
    }

    /**
     * Handles load game requests from the game engine
     */
    public void handleLoadGameRequest() {
        showLoadGameDialog();
    }

    // Add these methods to support UIEventHandler
    public void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(message + "\n");
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
            // Clear input immediately after processing
            commandInput.setText("");
            commandInput.requestFocusInWindow();
        });
    }

    public void showPuzzleNotification(Object puzzleData) {
        SwingUtilities.invokeLater(() -> {
            if (puzzleData instanceof Puzzle) {
                outputArea.append("\nA puzzle is available in this room!\n");
                outputArea.append("Type 'solve' to attempt the puzzle.\n");
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            }
        });
    }

    public void updateCharacterStats(Player player) {
        SwingUtilities.invokeLater(() -> {
            if (player != null) {
                inventoryPanel.updateStatus(player);
            }
        });
    }

    public void updatePlayerPosition(String roomId) {
        SwingUtilities.invokeLater(() -> {
            if (mapPanel != null && roomId != null) {
                mapPanel.updatePlayerPosition(roomId);
                mapPanel.revalidate();
                mapPanel.repaint();
            }
        });
    }

    public void resetGameState() {
        SwingUtilities.invokeLater(() -> {
            // Clear output area
            outputArea.setText("");
            
            // Reset map panel
            if (mapPanel != null) {
                mapPanel.resetMap();
                Player player = GameEngine.getInstance().getPlayer();
                if (player != null && player.getLocation() != null) {
                    String roomId = player.getLocation().getRoomId();
                    mapPanel.revealRoom(roomId, player.getLocation().hasTreasure());
                    mapPanel.updatePlayerPosition(roomId);
                }
            }
            
            // Reset inventory panel
            if (inventoryPanel != null) {
                Player player = GameEngine.getInstance().getPlayer();
                if (player != null) {
                    updateInventory(); // Use the existing method
                } else {
                    inventoryPanel.reset(); // Use reset() instead of clearInventory()
                }
            }
        });
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }
} 
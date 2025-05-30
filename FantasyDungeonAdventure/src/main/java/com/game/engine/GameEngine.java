package com.game.engine ;
import com.game.database.GameDatabaseService;

import com.game.database.GameState;
import com.game.event.GameEventType;
import com.game.event.handlers.GameStateEventHandler;
import com.game.item.Item;
import com.game.level.Level;
import com.game.player.Player;
import com.game.room.Room;
import com.game.puzzle.Puzzle;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;
import java.util.Random;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import com.game.util.LogManager;

import com.game.gui.GameWindow;
import com.game.item.factory.ItemFactory;
import com.game.item.factory.DefaultItemFactory;

import javax.swing.JOptionPane;

import com.game.event.GameEventDispatcher;





/**
 * Core game engine class implementing the Singleton pattern.
 * Manages game state, processes commands, and coordinates all game systems.
 * This is the central hub that connects all game components and handles game flow.
 */
public class GameEngine implements Serializable {
    private static GameEngine instance;
    private final ItemFactory itemFactory;
    private Level currentLevel;
    private Player player;
    private String gameState;
    private static final Logger LOGGER = LogManager.getLogger(GameEngine.class.getName());
    private final transient Scanner scanner;
    private Random random;
    private List<Level> levels;
    private int currentLevelIndex;

    // Collection of helpful gameplay tips shown to players
    private static final String[] TIPS = {
        "Remember to check your inventory often!",
        "Some puzzles might require specific items to solve.",
        "Exploring thoroughly can reveal hidden passages and treasures.",
        "Your choices matter - they might affect the game's outcome!",
        "Don't forget to save your progress regularly.",
        "Use the 'look' command to examine your surroundings in detail.",
        "Stuck? Try using the 'hint' command for a random tip!",
        "The 'map' command shows where you've been - use it to avoid getting lost!",
        "Negative items can be dangerous, but sometimes risk brings great rewards.",
        "Solving puzzles can often yield valuable rewards or reveal secrets."
    };

    // Command aliases to improve user experience
    private static final Map<String, List<String>> COMMAND_ALIASES = new HashMap<>();
    static {
        // Map common variations of commands to their primary command
        COMMAND_ALIASES.put("move", Arrays.asList("go", "walk", "run", "travel"));
        COMMAND_ALIASES.put("look", Arrays.asList("examine", "inspect", "observe"));
        COMMAND_ALIASES.put("pickup", Arrays.asList("grab", "take", "collect"));
        COMMAND_ALIASES.put("inventory", Arrays.asList("inv", "items", "bag"));
        COMMAND_ALIASES.put("quit", Arrays.asList("exit", "leave", "end"));
        COMMAND_ALIASES.put("save", Arrays.asList("savegame", "store"));
        COMMAND_ALIASES.put("load", Arrays.asList("loadgame", "restore"));

        // Directional aliases for easier navigation
        COMMAND_ALIASES.put("north", Arrays.asList("n", "up", "forward", "forwards"));
        COMMAND_ALIASES.put("south", Arrays.asList("s", "down", "back", "backwards"));
        COMMAND_ALIASES.put("east", Arrays.asList("e", "right", "r"));
        COMMAND_ALIASES.put("west", Arrays.asList("w", "left", "l"));
    }


    /**
     * Private constructor to enforce Singleton pattern.
     * Initializes core game components and event handlers.
     *
     * @param itemFactory Factory for creating game items
     */
    private GameEngine(ItemFactory itemFactory) {
        this.itemFactory = itemFactory;
        this.gameState = "initialized";
        this.scanner = new Scanner(System.in);
        this.random = new Random();
        this.levels = new ArrayList<>();
        this.currentLevelIndex = 0;
        
        // Initialize command and event handling systems
        initializeCommandHandlers();
        GameStateEventHandler gameStateHandler = new GameStateEventHandler(this);
        GameEventDispatcher.getInstance().registerHandler(gameStateHandler);
    }

    /**
     * Gets the singleton instance of the game engine.
     * Creates a new instance with default item factory if none exists.
     *
     * @return The singleton GameEngine instance
     */
    public static synchronized GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine(new DefaultItemFactory());
        }
        return instance;
    }

    /**
     * Starts a new game session.
     * Displays introduction, handles difficulty selection, and initializes game state.
     */
    public void startGame() {
        try {
            gameState = "choosing_difficulty";
            displayGameIntroduction();
            chooseDifficulty();
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Failed to start game", e);
            fireEvent(GameEventType.GAME_MESSAGE, 
                "Error: Failed to start game. Please check the logs and restart.");
        }
    }

    /**
     * Processes player commands.
     * Validates game state, resolves command aliases, and routes to appropriate handler.
     *
     * @param command The raw command string from the player
     */
    public void processCommand(String command) {
        if (!validateGameState(command)) return;

        try {
            String[] parts = command.trim().toLowerCase().split("\\s+", 2);
            String action = resolveCommandAlias(parts[0]);
            String args = parts.length > 1 ? parts[1] : "";

            // Special handling for movement commands to prevent error messages on trap triggers
            if (action.equals("move") || Arrays.asList("north", "south", "east", "west").contains(action)) {
                if (action.equals("move")) {
                    handleMovement(args);
                } else {
                    handleMovement(action);
                }
                return;
            }

            CommandHandler handler = commandHandlers.get(action);
            if (handler != null) {
                handler.handle(args);
            } else {
                displayMessage("Unknown command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Error processing command: " + command, e);
            displayMessage("An error occurred processing your command.");
        }
    }

    private void displayGameIntroduction() {
        try {
            Thread.sleep(100);
            
            Map<String, String> welcomeInfo = new HashMap<>();
            welcomeInfo.put("", "You stand before an ancient dungeon, its mysteries beckoning...");
            welcomeInfo.put(" ", "As a brave adventurer, you must navigate through treacherous rooms,");
            welcomeInfo.put("  ", "solve puzzles, collect items, and overcome magical barriers.\n");
            displayGameStatus("Welcome to Fantasy Dungeon Game", welcomeInfo);
            
            Thread.sleep(500);
            
            Map<String, String> featuresInfo = new HashMap<>();
            featuresInfo.put("1", "Explore mysterious rooms with hidden passages");
            featuresInfo.put("2", "Collect and use magical items");
            featuresInfo.put("3", "Solve puzzles to unlock secrets");
            featuresInfo.put("4", "Overcome magical barriers using your power");
            featuresInfo.put("5", "Find the treasure room to complete each level\n");
            displayGameStatus("Game Features", featuresInfo);
            
            Thread.sleep(500);
            
            Map<String, String> tipsInfo = new HashMap<>();
            tipsInfo.put("1", "Use 'help' to see available commands");
            tipsInfo.put("2", "'look' to examine your surroundings");
            tipsInfo.put("3", "'status' to check your condition");
            tipsInfo.put("4", "Maintain your power points to overcome barriers");
            tipsInfo.put("5", "Collect items to increase your chances of survival\n");
            displayGameStatus("Tips", tipsInfo);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void chooseDifficulty() {

        String difficultyText = """

                === Choose Your Difficulty ===

                1. Easy:\s
                   • Start with 100 Power Points
                   • More healing items available
                   • Weaker barriers
                   • More hints for puzzles

                2. Normal:\s
                   • Start with 125 Power Points
                   • Balanced item distribution
                   • Standard barrier strength
                   • Regular puzzle difficulty

                3. Hard:\s
                   • Start with 150 Power Points
                   • Fewer healing items
                   • Stronger barriers
                   • More challenging puzzles

                Enter difficulty (1-3):\s""";
        fireEvent(GameEventType.GAME_MESSAGE, difficultyText);
        
        // Wait for player input
        gameState = "choosing_difficulty";
        
        // The actual difficulty selection will be handled in processCommand
    }

    /**
     * Adjusts various game parameters based on the selected difficulty level.
     * This method configures:
     * - Healing item spawn rates
     * - Barrier strength that players need to overcome
     * - Puzzle difficulty modifiers
     * <p>
     * For EASY:
     * - Higher healing item rate (60%)
     * - Weaker barriers (70% strength)
     * - Easier puzzles (80% difficulty)
     * <p>
     * For NORMAL:
     * - Balanced healing rate (40%)
     * - Standard barriers (100% strength)
     * - Standard puzzles (100% difficulty)
     * <p>
     * For HARD:
     * - Lower healing rate (20%)
     * - Stronger barriers (130% strength)
     * - Harder puzzles (120% difficulty)
     *
     * @param difficulty The selected difficulty level (EASY, NORMAL, or HARD)
     */
    private void adjustGameDifficulty(Player.DifficultyLevel difficulty) {
        switch (difficulty) {
            case EASY -> {
                itemFactory.setHealingItemRate(0.6f);
                itemFactory.setBarrierStrength(0.7f);
                Puzzle.setDifficultyModifier(0.8f);
            }
            case NORMAL -> {
                itemFactory.setHealingItemRate(0.4f);
                itemFactory.setBarrierStrength(1.0f);
                Puzzle.setDifficultyModifier(1.0f);
            }
            case HARD -> {
                itemFactory.setHealingItemRate(0.2f);
                itemFactory.setBarrierStrength(1.3f);
                Puzzle.setDifficultyModifier(1.2f);
            }
        }
        
        fireEvent(GameEventType.GAME_MESSAGE, "\nDifficulty set to: " + difficulty + 
            "\nYour adventure begins with " + player.getPowerPoints() + " Power Points.\n");
    }

    private interface CommandHandler {
        void handle(String args);
    }

    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();

    private void initializeCommandHandlers() {
        commandHandlers.put("move", args -> {
            if (!validateCommand("move", args, "Move where? Try: north, south, east, or west")) {
                return;
            }
            handleMovement(args);
        });
        
        commandHandlers.put("solve", args -> {
            if (args.isEmpty()) {
                solvePuzzle();
            } else {
                handlePuzzleAnswer(args);
            }
        });
        
        commandHandlers.put("reveal", args -> {
            Room currentRoom = player.getLocation();
            Room.RevealResult result = currentRoom.revealHiddenPassage(player, itemFactory);
            
            // Display the result message
            displayMessage(result.getMessage());
            
            // If successful, add the new room to the level and fire events
            if (result.isSuccess() && result.getNewRoom() != null) {
                currentLevel.getRooms().put(result.getNewRoom().getRoomId(), result.getNewRoom());
                fireEvent(GameEventType.ROOM_DISCOVERED, result.getNewRoom());
            }
        });
        
        commandHandlers.put("pickup", this::handlePickup);
        commandHandlers.put("take", this::handlePickup);
        commandHandlers.put("grab", this::handlePickup);
        
        commandHandlers.put("look", args -> lookAround());
        commandHandlers.put("inventory", args -> showInventory());
        commandHandlers.put("inv", args -> showInventory());
        commandHandlers.put("help", args -> displayHelp());
        commandHandlers.put("quit", args -> confirmQuit());
        commandHandlers.put("exit", args -> confirmQuit());
        
        commandHandlers.put("use", args -> {
            if (!validateCommand("use", args, "What do you want to use?")) {
                return;
            }
            handleUseItem(args);
        });
        
        commandHandlers.put("drop", args -> {
            if (!validateCommand("drop", args, "What do you want to drop?")) {
                return;
            }
            handleDropItem(args);
        });
        
        commandHandlers.put("hint", args -> displayRandomTip());
        commandHandlers.put("tips", args -> displayRandomTip());
        commandHandlers.put("status", args -> displayPlayerStatus());
        commandHandlers.put("save", args -> GameWindow.getInstance().handleSaveGameRequest(null));
        commandHandlers.put("load", args -> GameWindow.getInstance().handleLoadGameRequest());
        
        commandHandlers.put("map", args -> displayMap());
    }

    private boolean validateGameState(String command) {
        if ("choosing_difficulty".equals(gameState)) {
            handleDifficultySelection(command);
            return false;
        }
        if (!"running".equals(gameState)) {
            fireEvent(GameEventType.GAME_MESSAGE, "Please select a difficulty level first (1-3).");
            return false;
        }
        if (command == null || command.trim().isEmpty()) {
            fireEvent(GameEventType.GAME_MESSAGE, "Please enter a valid command.");
            return false;
        }
        return true;
    }

    private String resolveCommandAlias(String action) {
        for (Map.Entry<String, List<String>> entry : COMMAND_ALIASES.entrySet()) {
            if (entry.getValue().contains(action)) {
                return entry.getKey();
            }
        }
        return action;
    }

    private void handleDifficultySelection(String command) {
        if (command == null || command.trim().isEmpty()) {
            fireEvent(GameEventType.GAME_MESSAGE, "Please enter a number between 1-3: ");
            return;
        }
        
        String input = command.trim();
        Player.DifficultyLevel selectedDifficulty;

        switch (input) {
            case "1" -> selectedDifficulty = Player.DifficultyLevel.EASY;
            case "2" -> selectedDifficulty = Player.DifficultyLevel.NORMAL;
            case "3" -> selectedDifficulty = Player.DifficultyLevel.HARD;
            default -> {
                fireEvent(GameEventType.GAME_MESSAGE, "Please enter a valid number (1-3): ");
                return;
            }
        }
        
        // Initialize game with selected difficulty
        initializeGameWithDifficulty(selectedDifficulty);
        
        // Set game state to running AFTER initialization is complete
        gameState = "running";
        
        // Display initial tip
        displayRandomTip();
    }

    private void initializeGameWithDifficulty(Player.DifficultyLevel difficulty) {
        // Create new player with selected difficulty
        player = new Player(difficulty);
        
        // Adjust game parameters based on difficulty
        adjustGameDifficulty(difficulty);
        
        // Initialize game levels and starting position
        generateLevels();
        currentLevel = levels.get(currentLevelIndex);
        
        if (currentLevel != null && currentLevel.getStartingRoom() != null) {
            Room startingRoom = currentLevel.getStartingRoom();
            player.setLocation(startingRoom);
            
            // Enter the starting room
            startingRoom.enter(player);
            
            // Update UI with initial state
            fireEvent(GameEventType.ROOM_DISCOVERED, startingRoom);
            fireEvent(GameEventType.INVENTORY_CHANGED, player.getInventory());
        }
    }

    private void lookAround() {
        if (player == null || player.getLocation() == null) {
            fireEvent(GameEventType.GAME_MESSAGE, "Error: Player location not initialized.");
            return;
        }
        
        Room room = player.getLocation();
        StringBuilder lookText = new StringBuilder();
        
        // Display room description
        lookText.append(room.getDetailedDescription()).append("\n");
        
        // Display items only once
        List<Item> roomContents = room.getContents();
        if (!roomContents.isEmpty()) {
            lookText.append("\nYou see the following items:");
            for (Item item : roomContents) {
                lookText.append("\n- ").append(item.getName())
                       .append(": ").append(item.getDescription());
            }
        }
        
        fireEvent(GameEventType.GAME_MESSAGE, lookText.toString());
    }

    private void displayHelp() {
        Map<String, String> commandsInfo = new HashMap<>();
        commandsInfo.put("move/go [direction]", "Move in the specified direction (north, south, east, west)");
        commandsInfo.put("look", "Examine your surroundings in detail");
        commandsInfo.put("pickup/grab/take [item]", "Pick up an item");
        commandsInfo.put("use [item]", "Use an item from your inventory");
        commandsInfo.put("drop [item]", "Drop an item from your inventory");
        commandsInfo.put("inventory/inv", "Display your inventory");
        commandsInfo.put("map", "Display the map of explored areas");
        commandsInfo.put("status", "Display your current status");
        commandsInfo.put("solve", "Attempt to solve a puzzle in the room");
        commandsInfo.put("reveal", "Use a Torch to reveal hidden passages in the current room");
        commandsInfo.put("hint", "Get a random gameplay tip");
        commandsInfo.put("save", "Save your current game progress");
        commandsInfo.put("load", "Load a previously saved game");
        commandsInfo.put("quit", "Exit the game");
        commandsInfo.put("help", "Display this help message");
        displayGameStatus("Available Commands", commandsInfo);
    }

    private void confirmQuit() {
        int choice = JOptionPane.showConfirmDialog(
            GameWindow.getInstance(),
            "Are you sure you want to quit? Your unsaved progress will be lost.",
            "Confirm Quit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // Clean up and exit
            if (scanner != null) {
                scanner.close();
            }
            fireEvent(GameEventType.GAME_MESSAGE, "\nThanks for playing! Goodbye!");
            
            // Add a small delay to show the goodbye message
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Exit the application
            System.exit(0);
        }
    }

    private void solvePuzzle() {
        Room currentRoom = player.getLocation();
        Puzzle puzzle = currentRoom.getPuzzle();
        
        if (puzzle == null) {
            fireEvent(GameEventType.GAME_MESSAGE, "There's no puzzle in this room.");
            return;
        }
        
        if (puzzle.isSolved()) {
            fireEvent(GameEventType.GAME_MESSAGE, "You've already solved this puzzle!");
            return;
        }

        String puzzleText = "\n=== Puzzle Challenge ===\n" + puzzle.getDescription() + "\n" +
                "Question: " + puzzle.getQuestion() + "\n" +
                "\nType 'solve [your answer]' to submit your answer";
        
        fireEvent(GameEventType.GAME_MESSAGE, puzzleText);
    }

    private void handlePuzzleAnswer(String answer) {
        Room currentRoom = player.getLocation();
        Puzzle puzzle = currentRoom.getPuzzle();
        
        if (puzzle == null) {
            displayMessage("There's no puzzle in this room.");
            return;
        }
        
        if (puzzle.isSolved()) {
            displayMessage("This puzzle has already been solved!");
            return;
        }

        // Try to solve the puzzle with the provided answer
        if (puzzle.checkAnswer(answer.trim())) {
            puzzle.setSolved(true);
            player.adjustPowerPoints(10);
            
            // Consolidate puzzle completion messages and events
            StringBuilder message = new StringBuilder("\nCorrect! You've solved the puzzle!");
            message.append("\nYou gain 10 power points as a reward.");
            
            // Reveal a hidden item if present
            List<Item> roomContents = currentRoom.getContents();
            if (!roomContents.isEmpty()) {
                Item revealedItem = roomContents.get(random.nextInt(roomContents.size()));
                message.append("\nThe puzzle solution revealed a hidden ").append(revealedItem.getName()).append("!");
            }
            
            // Fire consolidated events
            displayMessage(message.toString());
            fireEvent(GameEventType.PUZZLE_SOLVED, puzzle);
            fireEvent(GameEventType.INVENTORY_CHANGED, player.getInventory());
        } else {
            player.adjustPowerPoints(-5);
            displayMessage("\nThat's not the correct answer. Try again!\nYou lose 5 power points.");
            checkPlayerHealth();  // Check health after power loss
        }
    }

    private void displayRandomTip() {
        String tip = TIPS[random.nextInt(TIPS.length)];
        fireEvent(GameEventType.GAME_MESSAGE, "\nTip: " + tip);
    }



    private int calculateScore() {
        int score = player.getPowerPoints();
        score += player.getVisitedRooms().size() * 10;
        score += player.getInventorySize() * 5;
        return score;
    }


    private void generateLevels() {
        int numberOfLevels = 3; // You can adjust this number
        for (int i = 1; i <= numberOfLevels; i++) {
            levels.add(new Level(i));
        }
    }



    // Getters and setters

    /**
     * @return Player return the player
     */
    public Player getPlayer() {
        return player;
    }


    /**
     * @return List<Level> return the levels
     */
    public List<Level> getLevels() {
        return levels;
    }

    /**
     * @return int return the currentLevelIndex
     */
    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public void fireEvent(GameEventType type, Object data) {
        GameEventDispatcher.getInstance().fireEvent(type, data);
    }

    private void handleMovement(String direction) {
        try {
            if (player == null || player.getLocation() == null) {
                fireEvent(GameEventType.GAME_MESSAGE, "Error: Player location not initialized.");
                return;
            }

            direction = direction.toLowerCase().trim();
            direction = convertDirection(direction);

            if (!Arrays.asList("north", "south", "east", "west").contains(direction)) {
                fireEvent(GameEventType.GAME_MESSAGE, "Invalid direction. Please use: north, south, east, or west");
                return;
            }

            Room currentRoom = player.getLocation();
            Room nextRoom = currentRoom.getConnectedRoom(direction);

            if (nextRoom != null) {
                // First, try to enter the room and handle traps/barriers
                nextRoom.enter(player);
                
                // Check if player survived the traps/barriers
                if (player.getPowerPoints() > 0) {
                    // If player survived, complete the movement
                    player.setLocation(nextRoom);
                    fireEvent(GameEventType.ROOM_ENTERED, nextRoom);
                    fireEvent(GameEventType.PLAYER_MOVED, nextRoom);
                    
                    if (nextRoom.hasTreasure()) {
                        handleTreasureRoomDiscovery();
                    }
                }
                // Always check player health after trap/barrier interaction
                checkPlayerHealth();
            } else {
                fireEvent(GameEventType.GAME_MESSAGE, "You cannot go " + direction + " from here. Available exits: " + 
                    String.join(", ", currentRoom.getExits()));
            }
        } catch (Exception e) {
            // Log the error but don't display it to the user since trap messages are handled separately
            LOGGER.log(java.util.logging.Level.WARNING, "Error in movement handling: " + e.getMessage(), e);
        }
    }

    private String convertDirection(String dir) {
        return switch (dir) {
            case "n" -> "north";
            case "s" -> "south";
            case "e" -> "east";
            case "w" -> "west";
            default -> dir;
        };
    }

    public void exitGame() {
        System.exit(0);
    }

    private void handlePickup(String itemName) {
        Room currentRoom = player.getLocation();
        Item item = currentRoom.findItemByName(itemName);
        
        if (item != null) {
            player.pickUp(item);
            fireEvent(GameEventType.INVENTORY_CHANGED, player.getInventory());
        } else {
            fireEvent(GameEventType.GAME_MESSAGE, "There is no " + itemName + " here.");
        }
    }

    private void showInventory() {
        List<Item> items = player.getInventory();
        if (items.isEmpty()) {
            fireEvent(GameEventType.GAME_MESSAGE, "Your inventory is empty.");
        } else {
            StringBuilder sb = new StringBuilder("\nYour inventory contains:");
            for (Item item : items) {
                sb.append("\n- ").append(item.getName());
            }
            fireEvent(GameEventType.GAME_MESSAGE, sb.toString());
        }
    }

    private void handleUseItem(String itemName) {
        handleInventoryAction(itemName, "use", item -> {
            fireEvent(GameEventType.GAME_MESSAGE, "\nUsing " + item.getName() + "...");
            
            int beforeHealth = player.getPowerPoints();
            boolean consumed = item.use(player);
            int afterHealth = player.getPowerPoints();
            
            int difference = Math.abs(afterHealth - beforeHealth);
            if (difference > 0) {
                fireEvent(GameEventType.GAME_MESSAGE, afterHealth > beforeHealth ? 
                    "You gained " + difference + " power points!" :
                    "You lost " + difference + " power points!");
            }
            
            if (consumed) {
                player.getInventory().remove(item);
                fireEvent(GameEventType.GAME_MESSAGE, "The " + item.getName() + " was consumed.");
                fireEvent(GameEventType.INVENTORY_CHANGED, player.getInventory());
            }
            
            checkPlayerHealth();
        });
    }

    private void handleInventoryAction(String itemName, String action, java.util.function.Consumer<Item> operation) {
        if (itemName == null || itemName.trim().isEmpty()) {
            fireEvent(GameEventType.GAME_MESSAGE, "What item do you want to " + action + "?");
            return;
        }
        
        Item item = player.getInventory().stream()
            .filter(i -> i.getName().equalsIgnoreCase(itemName))
            .findFirst()
            .orElse(null);
            
        if (item != null) {
            operation.accept(item);
        } else {
            fireEvent(GameEventType.GAME_MESSAGE, "You don't have a " + itemName + " in your inventory.");
        }
    }

    private void handleDropItem(String itemName) {
        handleInventoryAction(itemName, "drop", item -> {
            player.getInventory().remove(item);
            player.getLocation().addItem(item);
            fireEvent(GameEventType.INVENTORY_CHANGED, player.getInventory());
        });
    }

    private void displayPlayerStatus() {
        if (player != null) {
            Map<String, String> statusInfo = new HashMap<>();
            statusInfo.put("Health", player.getPowerPoints() + " HP");
            statusInfo.put("Status", player.getStatus());
            statusInfo.put("Current Location", "Room " + player.getLocation().getRoomId());
            statusInfo.put("Difficulty", player.getDifficultyLevel().toString());
            statusInfo.put("Rooms Explored", String.valueOf(player.getVisitedRooms().size()));
            statusInfo.put("Items in Inventory", String.valueOf(player.getInventory().size()));
            
            displayGameStatus("Player Status", statusInfo);
        }
    }

    // Getter for the item factory
    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    private void checkPlayerHealth() {
        if (player.getPowerPoints() <= 0) {
            fireEvent(GameEventType.GAME_MESSAGE, "\n=== GAME OVER ===");
            fireEvent(GameEventType.GAME_MESSAGE, "Your power has been depleted!");
            
            int choice = JOptionPane.showConfirmDialog(
                null,
                "Would you like to restart the level?",
                "Game Over",
                JOptionPane.YES_NO_OPTION
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                restartLevel();
            } else {
                confirmQuit();
            }
        }
    }
    /**
     * Restarts the current level of the game.
     * This method resets the player's health, regenerates the current level,
     * places the player at the starting room, and updates the UI components.
     * It's typically called when the player's health reaches zero or when
     * the player chooses to restart the level.
     * <p>
     * This method performs the following actions:
     * 1. Resets the player's health
     * 2. Regenerates the current level
     * 3. Places the player at the starting room
     * 4. Refreshes the UI
     * 5. Updates UI components with new game state
     * 6. Resets the game state to "running"
     * 7. Updates the player status display
     * <p>
     * No parameters are required as it uses the current game state.
     * No return value as it modifies the game state directly.
     */
    private void restartLevel() {
        // Generate a new level to reset all room states
        Level newLevel = new Level(currentLevelIndex + 1);
        levels.set(currentLevelIndex, newLevel);
        currentLevel = newLevel;
        
        // Reset player state
        player.resetHealth();
        player.getVisitedRooms().clear();
        player.getInventory().clear();
        player.setLocation(currentLevel.getStartingRoom());
        
        // Reset GUI
        GameWindow.getInstance().resetGameState();
        
        // Mark game as running
        gameState = "running";
        
        // Reveal starting room and update UI
        Room startingRoom = currentLevel.getStartingRoom();
        fireEvent(GameEventType.ROOM_DISCOVERED, startingRoom);
        fireEvent(GameEventType.INVENTORY_CHANGED, player.getInventory());
        
        // Display restart messages
        String message = "\n=== Level Restarted ===" + "\nYour power has been restored to " + player.getPowerPoints() + " points." +
                "\n" + startingRoom.getDescription();
        
        fireEvent(GameEventType.GAME_MESSAGE, message);
        displayRandomTip();  // Give the player a helpful tip after restart
    }

    public void loadGame(String saveName) {
        try {
            GameDatabaseService dbService = new GameDatabaseService();
            GameState loadedState = dbService.loadGameState(saveName);
            if (loadedState != null) {
                this.player = loadedState.getPlayer();
                this.levels = loadedState.getLevels();
                this.currentLevelIndex = loadedState.getCurrentLevelIndex();
                this.currentLevel = levels.get(currentLevelIndex);
                
                // Reset the GUI state
                GameWindow.getInstance().resetGameState();
                
                // Update map with all visited rooms
                Set<Room> visitedRooms = player.getVisitedRooms();
                for (Room room : visitedRooms) {
                    // Reveal each visited room and mark if it's a treasure room
                    GameWindow.getInstance().getMapPanel().revealRoom(room.getRoomId(), room.hasTreasure());
                }
                
                // Update current player position
                Room currentRoom = player.getLocation();
                if (currentRoom != null) {
                    // Update player position on map
                    GameWindow.getInstance().getMapPanel().updatePlayerPosition(currentRoom.getRoomId());
                    
                    // Show game loaded message and room description
                    fireEvent(GameEventType.GAME_MESSAGE, "\nGame loaded successfully.");
                    fireEvent(GameEventType.GAME_MESSAGE, currentRoom.getDescription());
                    
                    // Update inventory display
                    fireEvent(GameEventType.INVENTORY_CHANGED, player.getInventory());
                }
                
                // Force map panel to repaint
                GameWindow.getInstance().getMapPanel().repaint();
            } else {
                fireEvent(GameEventType.GAME_MESSAGE, 
                    "Save file is incompatible with current game version. Starting new game...");
                startGame();
            }
        } catch (Exception e) {
            LOGGER.severe("Error loading game: " + e.getMessage());
            fireEvent(GameEventType.GAME_MESSAGE, 
                "Error loading game. Starting new game...");
            startGame();
        }
    }

    public void handleMovementEvent(Room room) {
        if (room != null) {
            player.setLocation(room);
            checkPlayerHealth();
        }
    }

    public void handlePuzzleCompletion(Puzzle puzzle) {
        if (puzzle != null && puzzle.isSolved()) {
            // Handle puzzle completion rewards
            player.adjustPowerPoints(10);
            fireEvent(GameEventType.GAME_MESSAGE, "Puzzle solved! You gain power points.");
            fireEvent(GameEventType.INVENTORY_CHANGED, player.getInventory());
        }
    }

    public void handleGameEnd() {
        gameState = "ended";
        // Handle any cleanup or final score calculations
    }

    // Helper method for command validation
    private boolean validateCommand(String command, String args, String errorMessage) {
        if (args == null || args.trim().isEmpty()) {
            fireEvent(GameEventType.GAME_MESSAGE, errorMessage);
            return false;  // Return false when validation fails
        }
        return true;  // Return true when validation succeeds
    }

    // Consolidated message display method
    private void displayMessage(String message, String... additionalLines) {
        StringBuilder sb = new StringBuilder(message);
        for (String line : additionalLines) {
            sb.append("\n").append(line);
        }
        LOGGER.info(sb.toString());
        fireEvent(GameEventType.GAME_MESSAGE, sb.toString());
    }

    // Consolidated game state display method
    private void displayGameStatus(String title, Map<String, String> statusInfo) {
        StringBuilder sb = new StringBuilder("\n=== " + title + " ===");
        statusInfo.forEach((key, value) -> sb.append("\n").append(key).append(": ").append(value));
        displayMessage(sb.toString());
    }

    private void displayMap() {
        if (player == null || player.getLocation() == null) {
            fireEvent(GameEventType.GAME_MESSAGE, "Error: Player location not initialized.");
            return;
        }

        // Update the map display
        fireEvent(GameEventType.ROOM_DISCOVERED, player.getLocation());
        
        // Show map legend and current position
        String mapInfo = "\n=== Map Information ===" + "\nCurrent Position: Room " + player.getLocation().getRoomId() +
                "\nExplored Rooms: " + player.getVisitedRooms().size() +
                "\nAvailable Exits: " + String.join(", ", player.getLocation().getExits());
        
        fireEvent(GameEventType.GAME_MESSAGE, mapInfo);
    }

    private void handleTreasureRoomDiscovery() {
        // Mark current level as completed
        currentLevel.setCompleted(true);
        
        // Calculate bonus points for finding treasure
        int treasureBonus = 50 + (player.getPowerPoints() / 2);
        player.adjustPowerPoints(treasureBonus);
        
        StringBuilder message = new StringBuilder("\n=== TREASURE ROOM DISCOVERED! ===");
        message.append("\nCongratulations! You've found the treasure room!");
        message.append("\nYou receive ").append(treasureBonus).append(" bonus power points!");
        
        // Check if there are more levels
        if (currentLevelIndex < levels.size() - 1) {
            message.append("\n\nPreparing for next level...");
            fireEvent(GameEventType.GAME_MESSAGE, message.toString());
            
            // Progress to next level
            currentLevelIndex++;
            currentLevel = levels.get(currentLevelIndex);
            
            // Ask player if they want to continue
            int choice = JOptionPane.showConfirmDialog(
                GameWindow.getInstance(),
                "Would you like to proceed to level " + (currentLevelIndex + 1) + "?",
                "Level Complete!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                // Start next level
                startNextLevel();
            } else {
                // End game with victory
                endGameWithVictory();
            }
        } else {
            // Player has completed all levels
            message.append("\n\nCONGRATULATIONS! You've completed all levels!");
            fireEvent(GameEventType.GAME_MESSAGE, message.toString());
            endGameWithVictory();
        }
    }

    private void startNextLevel() {
        // Reset visited rooms but keep inventory and power points
        player.getVisitedRooms().clear();
        
        // Place player at new level's starting room
        Room startingRoom = currentLevel.getStartingRoom();
        player.setLocation(startingRoom);
        
        // Reset GUI for new level
        GameWindow.getInstance().resetGameState();
        
        // Show new level introduction
        String message = "\n=== LEVEL " + (currentLevelIndex + 1) + " ===" + "\nYou enter a new section of the dungeon..." +
                "\n" + startingRoom.getDescription();
        
        fireEvent(GameEventType.GAME_MESSAGE, message);
        fireEvent(GameEventType.ROOM_DISCOVERED, startingRoom);
        fireEvent(GameEventType.INVENTORY_CHANGED, player.getInventory());
        
        displayRandomTip();
    }

    private void endGameWithVictory() {
        StringBuilder message = new StringBuilder("\n=== GAME COMPLETE! ===");
        message.append("\nYou've successfully completed your dungeon adventure!");
        message.append("\nFinal Score: ").append(calculateScore());
        message.append("\nPower Points: ").append(player.getPowerPoints());
        message.append("\nRooms Explored: ").append(player.getVisitedRooms().size());
        message.append("\nItems Collected: ").append(player.getInventorySize());
        
        fireEvent(GameEventType.GAME_MESSAGE, message.toString());
        
        // Show victory dialog
        int choice = JOptionPane.showConfirmDialog(
            GameWindow.getInstance(),
            message.toString() + "\n\nWould you like to play again?",
            "Victory!",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // Reset and start new game
            instance = null;
            GameWindow.getInstance().clearOutput();
            getInstance().startGame();
        } else {
            // Exit game
            exitGame();
        }
    }

}

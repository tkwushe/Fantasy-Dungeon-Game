package com.game.level;

import com.game.room.Room;
import com.game.item.Item;
import com.game.item.NegativeItem;
import com.game.puzzle.Puzzle;
import com.game.engine.GameEngine;
import com.game.player.Player;
import com.game.item.HealingItem;
import com.game.item.ToolItem;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * The Level class represents a dungeon level in the game.
 * It is responsible for generating rooms, connecting them,
 * placing items, and ensuring there is a path to the treasure.
 */
public class Level implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, Room> rooms; // Key: room ID, Value: Room object
    private Room startingRoom;
    private Room treasureRoom;
    private final int width;
    private final int height;
    private final Random random = new Random();

    // Constants for level dimensions
    private static final int EASY_MIN_SIZE = 4;
    private static final int EASY_MAX_SIZE = 7;
    private static final int NORMAL_MIN_SIZE = 5;
    private static final int NORMAL_MAX_SIZE = 10;
    private static final int HARD_MIN_SIZE = 7;
    private static final int HARD_MAX_SIZE = 12;

    // Add difficulty-based probabilities
    private static final Map<Player.DifficultyLevel, DifficultySettings> DIFFICULTY_SETTINGS = Map.of(
            Player.DifficultyLevel.EASY, new DifficultySettings(20, 15, 15),    // items, puzzles, hidden
            Player.DifficultyLevel.NORMAL, new DifficultySettings(30, 20, 10),
            Player.DifficultyLevel.HARD, new DifficultySettings(40, 25, 5)
    );

    private static class DifficultySettings {
        final int itemChance;      // Chance for items in room
        final int puzzleChance;    // Chance for puzzles
        final int hiddenChance;    // Chance for hidden passages

        DifficultySettings(int itemChance, int puzzleChance, int hiddenChance) {
            this.itemChance = itemChance;
            this.puzzleChance = puzzleChance;
            this.hiddenChance = hiddenChance;
        }
    }

    /**
     * Constructor for Level.
     *
     * @param levelNumber The number of the level (e.g., 1, 2, 3).
     */
    public Level(int levelNumber) {
        this.rooms = new HashMap<>();
        this.width = generateRandomSize();
        this.height = generateRandomSize();
        generateRooms();
    }

    /**
     * Generates a random size for the level dimensions within predefined limits.
     *
     * @return A random integer between MIN_SIZE and MAX_SIZE.
     */
    private int generateRandomSize() {
        Player.DifficultyLevel difficulty = GameEngine.getInstance().getPlayer().getDifficultyLevel();
        int minSize, maxSize;

        switch (difficulty) {
            case EASY -> {
                minSize = EASY_MIN_SIZE;
                maxSize = EASY_MAX_SIZE;
            }
            case HARD -> {
                minSize = HARD_MIN_SIZE;
                maxSize = HARD_MAX_SIZE;
            }
            default -> { // NORMAL
                minSize = NORMAL_MIN_SIZE;
                maxSize = NORMAL_MAX_SIZE;
            }
        }

        return new Random().nextInt((maxSize - minSize) + 1) + minSize;
    }

    /**
     * Generates rooms, connects them, places items, and ensures there is a path to the treasure.
     */
    public void generateRooms() {
        Room[][] grid = new Room[height][width];
        Player.DifficultyLevel difficulty = GameEngine.getInstance().getPlayer().getDifficultyLevel();
        DifficultySettings settings = DIFFICULTY_SETTINGS.get(difficulty);

        // Create rooms
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                String roomId = x + "," + y;
                String name = "Room " + roomId;
                String description = generateRoomDescription();
                String detailedDescription = generateDetailedDescription();

                Room room = new Room(roomId, name, description, detailedDescription);
                grid[y][x] = room;
                rooms.put(roomId, room);

                // Add random items based on difficulty
                if (random.nextInt(100) < settings.itemChance) {
                    // 30% chance for negative items in harder difficulties
                    if (difficulty != Player.DifficultyLevel.EASY && random.nextInt(100) < 30) {
                        room.addItem(GameEngine.getInstance().getItemFactory().createRandomNegativeItem());
                    } else {
                        Item item = GameEngine.getInstance().getItemFactory().createRandomItem();
                        room.addItem(item);
                    }
                }

                // Add puzzles based on difficulty
                if (random.nextInt(100) < settings.puzzleChance) {
                    room.setPuzzle(Puzzle.generateRandomPuzzle());
                }

                // Set hidden passages based on difficulty
                if (random.nextInt(100) < settings.hiddenChance) {
                    room.setHasHiddenPassages(true);
                }
            }
        }

        // Set special rooms
        startingRoom = grid[0][0];

        // Place treasure room strategically
        placeTreasureRoom(grid);

        // Connect rooms
        connectRooms(grid);

        // Create maze-like structure with barriers (walls)
        createMazeWithBarriers(grid, difficulty);

        // Ensure path exists
        ensurePathToTreasure(grid);
    }

    private void connectRooms(Room[][] grid) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Room currentRoom = grid[y][x];

                // Connect to adjacent rooms
                if (y > 0) currentRoom.connectRoom("north", grid[y - 1][x]);
                if (y < height - 1) currentRoom.connectRoom("south", grid[y + 1][x]);
                if (x > 0) currentRoom.connectRoom("west", grid[y][x - 1]);
                if (x < width - 1) currentRoom.connectRoom("east", grid[y][x + 1]);
            }
        }
    }

    private void placeTreasureRoom(Room[][] grid) {
        // Place treasure room in the far half of the grid
        int minDistance = Math.max(width, height) / 2; // Minimum distance from start

        int tx, ty;
        do {
            tx = random.nextInt(width);
            ty = random.nextInt(height);
        } while (manhattanDistance(0, 0, tx, ty) < minDistance);

        treasureRoom = grid[ty][tx];
        treasureRoom.setTreasure(true);

        // Add valuable treasure items

        // Add a powerful healing item
        HealingItem healingTreasure = new HealingItem(
                "Legendary Healing Crystal",
                "A rare crystal pulsing with restorative energy",
                50, // High healing amount
                false // Not food
        );
        treasureRoom.addItem(healingTreasure);

        // Add a powerful tool/spell
        ToolItem toolTreasure = new ToolItem(
                "Ancient Mystic Staff",
                "A powerful magical artifact from a forgotten age",
                30, // High power
                true, // Is spell
                true  // Can reveal passages
        );
        treasureRoom.addItem(toolTreasure);
    }

    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private void createMazeWithBarriers(Room[][] grid, Player.DifficultyLevel difficulty) {
        int barrierDensity = switch (difficulty) {
            case EASY -> 20;    // 20% of paths blocked
            case NORMAL -> 35;  // 35% of paths blocked
            case HARD -> 50;    // 50% of paths blocked
        };

        int negativeItemChance = switch (difficulty) {
            case EASY -> 10;    // 10% chance for negative items
            case NORMAL -> 25;  // 25% chance for negative items
            case HARD -> 40;    // 40% chance for negative items
        };

        // Create strategic barrier walls
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Room room = grid[y][x];

                // Skip start and treasure rooms
                if (room == startingRoom || room == treasureRoom) continue;

                // Create barrier walls more frequently in the middle of the map
                boolean isMiddleArea = x > width / 4 && x < (width * 3) / 4 &&
                        y > height / 4 && y < (height * 3) / 4;

                if (isMiddleArea) {
                    barrierDensity += 20; // Increase density in middle area
                    negativeItemChance += 15; // More dangerous in middle area
                }

                // Place barriers (walls) strategically
                if (random.nextInt(100) < barrierDensity) {
                    NegativeItem barrier = GameEngine.getInstance().getItemFactory().createBarrier();
                    room.setBarrier(barrier);
                }

                // Add negative items independently of barriers
                if (random.nextInt(100) < negativeItemChance) {
                    room.addItem(GameEngine.getInstance().getItemFactory().createRandomNegativeItem());
                }

                // Reset densities for next room
                barrierDensity = switch (difficulty) {
                    case EASY -> 20;
                    case NORMAL -> 35;
                    case HARD -> 50;
                };
                negativeItemChance = switch (difficulty) {
                    case EASY -> 10;
                    case NORMAL -> 25;
                    case HARD -> 40;
                };
            }
        }
    }

    private void ensurePathToTreasure(Room[][] grid) {
        // First try to find existing path
        if (!hasPath(startingRoom, treasureRoom, new HashSet<>())) {
            // If no path exists, create a challenging path
            createChallengingPath(grid);
        }
    }

    private boolean hasPath(Room start, Room target, Set<Room> visited) {
        if (start == target) return true;
        visited.add(start);

        for (String direction : start.getExits()) {
            Room next = start.getConnectedRoom(direction);
            if (next != null && !visited.contains(next) && !next.hasBarrier()) {
                if (hasPath(next, target, visited)) return true;
            }
        }

        return false;
    }

    private void createChallengingPath(Room[][] grid) {
        // Get coordinates of start and treasure rooms
        int[] start = getRoomCoordinates(startingRoom);
        int[] end = getRoomCoordinates(treasureRoom);

        // Create a winding path between them
        List<Room> pathRooms = new ArrayList<>();
        int x = start[0], y = start[1];

        while (x != end[0] || y != end[1]) {
            Room current = grid[y][x];
            pathRooms.add(current);

            // Decide whether to move horizontally or vertically
            if (random.nextBoolean() && x != end[0]) {
                x += (x < end[0]) ? 1 : -1;
            } else if (y != end[1]) {
                y += (y < end[1]) ? 1 : -1;
            } else {
                x += (x < end[0]) ? 1 : -1;
            }

            // Add some randomness to the path
            if (random.nextInt(100) < 30) { // 30% chance to add a detour
                if (random.nextBoolean() && y > 0) y--;
                else if (y < height - 1) y++;
            }
        }

        // Clear barriers along the path and add connections
        for (Room room : pathRooms) {
            room.setBarrier(null);
        }
    }

    private int[] getRoomCoordinates(Room room) {
        String[] coords = room.getRoomId().split(",");
        return new int[]{Integer.parseInt(coords[0]), Integer.parseInt(coords[1])};
    }

    private String generateRoomDescription() {
        String[] descriptions = {
                "You are in a dark room.",
                "You find yourself in a dimly lit chamber.",
                "You enter a mysterious room with ancient markings.",
                "This room is filled with echoes of the past.",
                "A cold draft blows through this shadowy room."
        };
        return descriptions[new Random().nextInt(descriptions.length)];
    }

    private String generateDetailedDescription() {
        String[] details = {
                "This is a dimly lit room with rough stone walls. You can barely make out the outlines of the room in the flickering light.",
                "Ancient runes cover the walls of this chamber, glowing faintly in the darkness.",
                "Cobwebs hang from the ceiling, and the air is thick with dust and mystery.",
                "The stone floor is worn smooth by countless footsteps of those who came before.",
                "Strange symbols are etched into the walls, their meaning lost to time."
        };
        return details[new Random().nextInt(details.length)];
    }

    /**
     * Gets a room based on its ID.
     *
     * @param roomId The ID of the room (e.g., "0,0").
     * @return The Room object, or null if not found.
     */
    public Room getRoom(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return null;
        }
        return rooms.get(roomId);
    }

    /**
     * Gets the starting room of the level.
     *
     * @return The starting Room.
     */
    public Room getStartingRoom() {
        return startingRoom;
    }

    /**
     * Gets the collection of rooms in the level.
     *
     * @return A Map of room IDs to Room objects.
     */
    public Map<String, Room> getRooms() {
        return rooms;
    }

    public void setCompleted(boolean completed) {

    }


}


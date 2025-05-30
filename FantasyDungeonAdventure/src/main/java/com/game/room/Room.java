package com.game.room;

import com.game.item.*;
import com.game.puzzle.Puzzle;
import com.game.player.Player;
import com.game.engine.GameEngine;
import com.game.event.GameEventType;
import com.game.item.factory.ItemFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Room implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Random random = new Random();

    // Core room properties
    private final String roomId;
    private final String name;
    private final String description;
    private final String detailedDescription;
    private final List<Item> contents;
    private final Map<String, Room> connections;

    // Room state flags
    private boolean isVisited;
    private boolean hasTreasure;
    private boolean hasSpecialTreasure;
    private boolean hasHiddenPassages;
    private boolean secretsRevealed;
    private NegativeItem barrier;
    private Puzzle puzzle;

    public Room(String roomId, String name, String description, String detailedDescription) {
        this.roomId = roomId;
        this.name = name;
        this.description = description;
        this.detailedDescription = detailedDescription;
        this.contents = new ArrayList<>();
        this.connections = new HashMap<>();
        
        if (random.nextInt(100) < 30) {
            populateRandomItems();
        }
    }

    private void populateRandomItems() {
        GameEngine engine = GameEngine.getInstance();
        int numItems = random.nextInt(3) + 1;
        
        // Add regular items
        for (int i = 0; i < numItems; i++) {
            Item item = engine.getItemFactory().createRandomItem();
            if (!(item instanceof NegativeItem)) {
                contents.add(item);
            }
        }
        
        // Add special item (5% chance)
        if (random.nextInt(100) < 5) {
            contents.add(createSpecialItem());
        }
    }

    private Item createSpecialItem() {
        return random.nextBoolean() ?
            new HealingItem("Rare Healing Crystal", "A crystal containing healing energy", 35, false) :
            new ToolItem("Enchanted Artifact", "A mysterious magical item of great power", 20, true, true);
    }

    // Room connections and navigation
    public void connectRoom(String direction, Room room) {
        if (direction != null && !direction.trim().isEmpty() && room != null) {
            connections.put(direction.toLowerCase(), room);
        }
    }

    public Room getConnectedRoom(String direction) {
        return direction != null ? connections.get(direction.toLowerCase()) : null;
    }

    public List<String> getExits() {
        return new ArrayList<>(connections.keySet());
    }

    // Item management
    public void addItem(Item item) {
        if (item != null) {
            contents.add(item);
            if (isVisited) {
                GameEngine.getInstance().fireEvent(GameEventType.GAME_MESSAGE, 
                    "A " + item.getName() + " appears in the room.");
            }
        }
    }

    public void removeItem(Item item) {
        if (item != null && contents.remove(item)) {
            GameEngine.getInstance().fireEvent(GameEventType.GAME_MESSAGE, 
                "The " + item.getName() + " was removed from the room.");
        }
    }

    public List<Item> getContents() {
        return contents.stream()
            .filter(item -> !(item instanceof NegativeItem) || 
                   (item instanceof NegativeItem && !((NegativeItem)item).isDefeated()))
            .collect(Collectors.toList());
    }

    public Item findItemByName(String itemName) {
        return itemName != null ? contents.stream()
            .filter(item -> item.getName().equalsIgnoreCase(itemName.trim()))
            .findFirst()
            .orElse(null) : null;
    }

    // Room description generation
    public String getDescription() {
        StringBuilder sb = new StringBuilder(description)
            .append("\nExits: ").append(String.join(", ", getExits()));

        if (hasBarrier() && !barrier.isDefeated()) {
            sb.append("\nA ").append(barrier.getName())
              .append(" blocks further progress. (Required Power: ")
              .append(barrier.getDamage() * 2).append(")");
        }

        if (hasPuzzle() && !puzzle.isSolved()) {
            sb.append("\nThere's an unsolved puzzle in this room: ")
              .append(puzzle.getDescription());
        }

        if (!contents.isEmpty()) {
            sb.append("\nYou see the following items in the room:");
            contents.forEach(item -> 
                sb.append("\n- ").append(item.getName())
                  .append(": ").append(item.getDescription()));
        }

        if (hasHiddenPassages && !secretsRevealed) {
            sb.append("\nYou sense there might be hidden secrets in this room...");
        }

        if (hasTreasure || hasSpecialTreasure) {
            sb.append("\nThere is a").append(hasSpecialTreasure ? " special" : "")
              .append(" treasure here!");
        }

        return sb.toString();
    }

    // Room entry and effects
    public void enter(Player player) {
        RoomEventInfo eventInfo = new RoomEventInfo(this, player);
        
        if (!isVisited) {
            handleFirstVisit(eventInfo);
        } else {
            handleRevisit(eventInfo);
        }

        handleBarrierAndTraps(player, eventInfo);
        
        GameEngine.getInstance().fireEvent(GameEventType.ROOM_ENTERED, eventInfo);
    }

    private void handleFirstVisit(RoomEventInfo eventInfo) {
        isVisited = true;
        eventInfo.addEvent(GameEventType.ROOM_DISCOVERED);
        eventInfo.setMessage(getDescription());
        
        contents.stream()
            .filter(item -> item instanceof HealingItem || 
                   (item instanceof ToolItem && ((ToolItem)item).isSpell()))
            .forEach(item -> GameEngine.getInstance().fireEvent(
                GameEventType.GAME_MESSAGE, 
                "You discovered a " + item.getName() + "!"));
    }

    private void handleRevisit(RoomEventInfo eventInfo) {
        StringBuilder message = new StringBuilder();
        
        if (hasBarrier() && !barrier.isDefeated()) {
            message.append("\nA ").append(barrier.getName())
                  .append(" blocks further progress. (Required Power: ")
                  .append(barrier.getDamage() * 2).append(")");
        }
        
        if (hasPuzzle() && !puzzle.isSolved()) {
            message.append("\nThere's an unsolved puzzle in this room: ")
                  .append(puzzle.getDescription());
        }
        
        if (message.length() > 0) {
            eventInfo.setMessage(message.toString());
        }
    }

    private void handleBarrierAndTraps(Player player, RoomEventInfo eventInfo) {
        if (hasBarrier() && !barrier.isDefeated()) {
            eventInfo.setBarrier(barrier);
            barrier.applyEffect(player);
        }

        // First, collect all traps that need to be processed
        List<NegativeItem> trapsToProcess = contents.stream()
            .filter(item -> item instanceof NegativeItem)
            .map(item -> (NegativeItem) item)
            .filter(trap -> !trap.isBarrier() && !trap.isDefeated())
            .toList();

        // Then process each trap
        for (NegativeItem trap : trapsToProcess) {
            trap.applyEffect(player);
            if (trap.isDefeated()) {
                contents.remove(trap);
            }
        }

        if (hasPuzzle() && !puzzle.isSolved()) {
            eventInfo.setPuzzle(puzzle);
            eventInfo.addEvent(GameEventType.PUZZLE_AVAILABLE);
        }
    }

    // Hidden passage handling
    public RevealResult revealHiddenPassage(Player player, ItemFactory itemFactory) {
        if (!hasHiddenPassages) {
            return new RevealResult(false, "There don't seem to be any hidden passages in this room.");
        }

        Item revealingItem = player.getInventory().stream()
            .filter(item -> item instanceof ToolItem && ((ToolItem) item).canRevealPassages())
            .findFirst()
            .orElse(null);

        if (revealingItem == null) {
            return new RevealResult(false, "You need a Torch or similar item to reveal hidden passages.");
        }

        if (!revealingItem.use(player)) {
            return new RevealResult(false, "The item failed to reveal any passages.");
        }

        List<String> availableDirections = new ArrayList<>(
            Arrays.asList("north", "south", "east", "west"));
        availableDirections.removeAll(getExits());
        
        if (availableDirections.isEmpty()) {
            return new RevealResult(false, "You search but find no new passages.");
        }

        String newDirection = availableDirections.get(random.nextInt(availableDirections.size()));
        Room newRoom = createHiddenRoom(newDirection, revealingItem.getName());
        newRoom.addItem(itemFactory.createRandomHealingItem());
        
        connectRoom(newDirection, newRoom);
        hasHiddenPassages = false;
        
        return new RevealResult(true, 
            "The " + revealingItem.getName() + " reveals a hidden passage to the " + newDirection + "!",
            newRoom, newDirection);
    }

    private Room createHiddenRoom(String direction, String revealerName) {
        String[] currentCoords = roomId.split(",");
        int x = Integer.parseInt(currentCoords[0]);
        int y = Integer.parseInt(currentCoords[1]);
        
        switch (direction) {
            case "north" -> y--;
            case "south" -> y++;
            case "east" -> x++;
            case "west" -> x--;
        }
        
        return new Room(x + "," + y, "Hidden Room",
            "You discovered a secret room!",
            "This hidden chamber was revealed by your " + revealerName + ".");
    }

    // Getters and setters (only for necessary properties)
    public String getRoomId() { return roomId; }
    public String getName() { return name; }
    public boolean isVisited() { return isVisited; }
    public boolean hasHiddenPassages() { return hasHiddenPassages; }
    public void setHasHiddenPassages(boolean value) { this.hasHiddenPassages = value; }
    public boolean hasTreasure() { return hasTreasure; }
    public void setTreasure(boolean value) { this.hasTreasure = value; }
    public boolean hasBarrier() { return barrier != null && !barrier.isDefeated(); }
    public NegativeItem getBarrier() { return barrier; }
    public void setBarrier(NegativeItem barrier) { this.barrier = barrier; }
    public boolean hasPuzzle() { return puzzle != null; }
    public Puzzle getPuzzle() { return puzzle; }
    public void setPuzzle(Puzzle puzzle) { this.puzzle = puzzle; }
    public String getDetailedDescription() { return detailedDescription; }

    // Inner class to hold room event information
    public static class RoomEventInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Room room;
        private final Player player;
        private final List<GameEventType> events;
        private NegativeItem barrier;
        private Puzzle puzzle;
        private String message;
        
        public RoomEventInfo(Room room, Player player) {
            this.room = room;
            this.player = player;
            this.events = new ArrayList<>();
            this.message = "";
        }

        public void setMessage(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void addEvent(GameEventType event) { events.add(event); }
        public void setBarrier(NegativeItem barrier) { this.barrier = barrier; }
        public void setPuzzle(Puzzle puzzle) { this.puzzle = puzzle; }
        public Room getRoom() { return room; }
        public Player getPlayer() { return player; }
        public List<GameEventType> getEvents() { return events; }
        public NegativeItem getBarrier() { return barrier; }
        public Puzzle getPuzzle() { return puzzle; }
    }

    // Inner class to hold reveal attempt results
    public static class RevealResult implements Serializable {
        private static final long serialVersionUID = 1L;
        private final boolean success;
        private final String message;
        private final Room newRoom;
        private final String direction;

        public RevealResult(boolean success, String message) {
            this(success, message, null, null);
        }

        public RevealResult(boolean success, String message, Room newRoom, String direction) {
            this.success = success;
            this.message = message;
            this.newRoom = newRoom;
            this.direction = direction;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Room getNewRoom() { return newRoom; }
        public String getDirection() { return direction; }
    }
}

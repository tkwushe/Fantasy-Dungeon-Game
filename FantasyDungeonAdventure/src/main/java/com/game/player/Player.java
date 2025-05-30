package com.game.player;

import com.game.item.Item;
import com.game.room.Room;

import java.io.Serial;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.io.Serializable;
import com.game.event.GameEventType;
import com.game.engine.GameEngine;
import java.util.ArrayList;

public class Player implements Serializable {
    @Serial
    private static final long serialVersionUID = -8588021223327765609L;
    private int powerPoints;
    private Room currentLocation;
    private final Inventory inventory;
    private final Set<Room> visitedRooms;
    private final DifficultyLevel difficultyLevel;
    private String status;
    private String name;
    private final List<String> activeEffects;

    public enum DifficultyLevel {
        EASY(100),
        NORMAL(75),
        HARD(150);

        private final int startingPowerPoints;

        DifficultyLevel(int startingPowerPoints) {
            this.startingPowerPoints = startingPowerPoints;
        }

        public int getStartingPowerPoints() {
            return startingPowerPoints;
        }
    }

    public Player(DifficultyLevel difficulty) {
        this.difficultyLevel = difficulty;
        this.inventory = new Inventory(20);
        this.visitedRooms = new HashSet<>();
        this.activeEffects = new ArrayList<>();
        
        // Set initial power points based on difficulty
        this.powerPoints = difficulty.getStartingPowerPoints();
        
        updateStatus();
    }

    public void move(String direction) {
        if (currentLocation != null) {
            Room nextRoom = currentLocation.getConnectedRoom(direction.toLowerCase());
            if (nextRoom != null) {
                setLocation(nextRoom);
            } else {
                throw new IllegalArgumentException("You cannot go that way.");
            }
        }
    }

    public void setLocation(Room room) {
        this.currentLocation = room;
        visitedRooms.add(room);
        room.enter(this);
    }

    public Room getLocation() {
        return currentLocation;
    }

    public List<Item> getInventory() {
        return inventory.getItems();
    }

    public int getInventorySize() {
        return inventory.getSize();
    }

    public Set<Room> getVisitedRooms() {
        return visitedRooms;
    }

    public int getPowerPoints() {
        return powerPoints;
    }

    public void adjustPowerPoints(int delta) {
        this.powerPoints += delta;
        updateStatus();
    }

    public String getStatus() {
        return status;
    }

    private void updateStatus() {
        if (powerPoints >= 75) {
            status = "Healthy";
        } else if (powerPoints >= 50) {
            status = "Wounded";
        } else if (powerPoints >= 25) {
            status = "Critical";
        } else {
            status = "Near Death";
        }
        
        // Add status-based effects
        clearEffects();
        if (powerPoints < 25) {
            addEffect("Weakened");
        }
        if (powerPoints < 50) {
            addEffect("Slowed");
        }
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void pickUp(Item item) {
        if (item != null && !inventory.isFull()) {
            if (inventory.addItem(item)) {
                currentLocation.removeItem(item);
                // Fire inventory changed event
                GameEngine.getInstance().fireEvent(GameEventType.INVENTORY_CHANGED, inventory.getItems());
            }
        }
    }

    public void useItem(String itemName) {
        Item item = inventory.findItemByName(itemName);
        if (item != null) {
            if (item.use(this)) {
                if (item.isConsumable()) {
                    inventory.removeItem(item);
                    // Fire inventory changed event
                    GameEngine.getInstance().fireEvent(GameEventType.INVENTORY_CHANGED, inventory.getItems());
                }
            }
        }
    }

    public void dropItem(String itemName) {
        Item item = inventory.findItemByName(itemName);
        if (item != null) {
            if (inventory.removeItem(item)) {
                currentLocation.addItem(item);
                // Fire inventory changed event
                GameEngine.getInstance().fireEvent(GameEventType.INVENTORY_CHANGED, inventory.getItems());
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void resetHealth() {
        // Reset power points based on current difficulty
        this.powerPoints = difficultyLevel.getStartingPowerPoints();
        
        // Clear any active effects
        clearEffects();
        updateStatus();
    }

    /**
     * Gets the list of active effects on the player.
     * @return List of active effect descriptions
     */
    public List<String> getActiveEffects() {
        return new ArrayList<>(activeEffects);  // Return a copy to prevent external modification
    }
    
    /**
     * Adds a new effect to the player.
     * @param effect The effect description to add
     */
    public void addEffect(String effect) {
        if (effect != null && !effect.trim().isEmpty()) {
            activeEffects.add(effect);
        }
    }
    
    /**
     * Removes an effect from the player.
     * @param effect The effect to remove
     */
    public void removeEffect(String effect) {
        activeEffects.remove(effect);
    }
    
    /**
     * Clears all active effects from the player.
     */
    public void clearEffects() {
        activeEffects.clear();
    }
}

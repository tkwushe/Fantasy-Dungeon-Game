package com.game.event.handlers;

import com.game.event.GameEvent;
import com.game.event.GameEventHandler;
import com.game.event.GameEventType;
import com.game.gui.GameWindow;
import com.game.room.Room;
import com.game.item.Item;
import java.util.EnumSet;
import java.util.Set;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class UIEventHandler implements GameEventHandler {
    private final GameWindow window;
    private static final Logger LOGGER = Logger.getLogger(UIEventHandler.class.getName());
    
    public UIEventHandler(GameWindow window) {
        this.window = window;
    }
    
    @Override
    public Set<GameEventType> getHandledEventTypes() {
        return EnumSet.of(
            GameEventType.ROOM_DISCOVERED,
            GameEventType.INVENTORY_CHANGED,
            GameEventType.GAME_MESSAGE,
            GameEventType.PLAYER_MOVED,
            GameEventType.PUZZLE_AVAILABLE,
            GameEventType.SECRET_DISCOVERED,
            GameEventType.PUZZLE_SOLVED,
            GameEventType.GAME_END,
            GameEventType.ROOM_ENTERED
        );
    }
    
    @Override
    public void handleEvent(GameEvent event) {
        if (!validateEvent(event)) return;
        
        try {
            dispatchEvent(event.getType(), event.getData());
        } catch (Exception e) {
            logError("Error handling event " + event.getType(), e);
        }
    }

    private boolean validateEvent(GameEvent event) {
        if (window == null) {
            logWarning("Window is null, cannot handle event: " + event.getType());
            return false;
        }
        return true;
    }

    private void dispatchEvent(GameEventType type, Object data) {
        switch (type) {
            case ROOM_DISCOVERED:
            case PLAYER_MOVED:
                handleRoomEvent(data, type == GameEventType.PLAYER_MOVED);
                break;
                
            case INVENTORY_CHANGED:
                handleInventoryChange(data);
                break;
                
            case GAME_MESSAGE:
                handleMessage(data);
                break;
                
            case PUZZLE_AVAILABLE:
                window.showPuzzleNotification(data);
                break;
                
            case SECRET_DISCOVERED:
            case GAME_END:
            case PUZZLE_SOLVED:
                handleSystemMessage(type);
                break;
                
            case ROOM_ENTERED:
                handleRoomEntered(data);
                break;
                
            default:
                logWarning("Unhandled event type: " + type);
                break;
        }
    }

    private void handleRoomEvent(Object data, boolean isMovement) {
        if (!(data instanceof Room)) return;
        
        Room room = (Room) data;
        if (isMovement) {
            window.updatePlayerPosition(room.getRoomId());
            updateGameState();
        } else {
            window.updateMap();
        }
    }

    private void handleInventoryChange(Object data) {
        if (!(data instanceof List<?>)) {
            logWarning("Invalid inventory data type: " + (data != null ? data.getClass().getName() : "null"));
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            List<Item> items = (List<Item>) data;
            updateGameState();
            logDebug("Inventory updated with " + items.size() + " items");
        } catch (ClassCastException e) {
            logWarning("Invalid inventory data received: " + e.getMessage());
        }
    }

    private void handleMessage(Object data) {
        if (data instanceof String) {
            window.displayMessage((String) data);
        }
    }

    private void handleSystemMessage(GameEventType type) {
        String message = switch (type) {
            case SECRET_DISCOVERED -> "You discovered a secret!";
            case GAME_END -> "Game Over!";
            case PUZZLE_SOLVED -> "Puzzle solved successfully!";
            default -> null;
        };
        
        if (message != null) {
            window.displayMessage(message);
            if (type == GameEventType.PUZZLE_SOLVED) {
                updateGameState();
            }
        }
    }

    private void handleRoomEntered(Object data) {
        if (!(data instanceof Room)) return;
        
        Room room = (Room) data;
        updateGameState();
        window.displayMessage(room.getDescription());
    }

    private void updateGameState() {
        window.updateMap();
        window.updateInventory();
        window.updateCharacterStats(null);
    }

    private void logWarning(String message) {
        LOGGER.warning(message);
    }

    private void logError(String message, Exception e) {
        LOGGER.log(Level.SEVERE, message, e);
    }

    private void logDebug(String message) {
        LOGGER.fine(message);
    }
} 
package com.game.event;

import java.util.*;
import java.util.logging.Logger;
/**
 * GameEventDispatcher implements the Observer pattern for event handling throughout the game.
 * This class serves as the central hub for all game events and their handlers.
 */
public class GameEventDispatcher {
    private static GameEventDispatcher instance;
    private final List<GameEventHandler> handlers;
    private static final Logger LOGGER = Logger.getLogger(GameEventDispatcher.class.getName());
 /**
     * Private constructor enforcing singleton pattern and initializing handler list.
     */
    private GameEventDispatcher() {
        handlers = new ArrayList<>();
    }
/**
     * Thread-safe singleton implementation to ensure only one event dispatcher exists.
     * @return The singleton instance of GameEventDispatcher
     */
    public static synchronized GameEventDispatcher getInstance() {
        if (instance == null) {
            instance = new GameEventDispatcher();
        }
        return instance;
    }
/**
     * Registers a new event handler to receive game events.
     * Ensures no duplicate handlers are registered.
     * @param handler The event handler to register
     */
    public void registerHandler(GameEventHandler handler) {
        if (handler != null && !handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    public void fireEvent(GameEventType type, Object data) {
        GameEvent event = new GameEvent(type, data);
        for (GameEventHandler handler : handlers) {
            try {
                if (handler.handlesEventType(type)) {
                    handler.handleEvent(event);
                }
            } catch (Exception e) {
                LOGGER.severe("Error handling event " + type + ": " + e.getMessage());
            }
        }
    }
} 
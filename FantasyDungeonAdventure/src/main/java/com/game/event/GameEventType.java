package com.game.event;

public enum GameEventType {
    // Player events
    PLAYER_MOVED,
    
    // Inventory events
    INVENTORY_CHANGED,
    
    // Room events
    ROOM_DISCOVERED,
    ROOM_ENTERED,
    
    // Puzzle events
    PUZZLE_AVAILABLE,
    PUZZLE_SOLVED,
    
    // Game state events
    GAME_MESSAGE,

    GAME_END,
    
    // Special events
    SECRET_DISCOVERED,
    
    // Save and load game events
    SAVE_GAME_REQUESTED,
    LOAD_GAME_REQUESTED
} 
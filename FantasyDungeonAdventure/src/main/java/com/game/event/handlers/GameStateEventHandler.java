package com.game.event.handlers;

import com.game.event.GameEvent;
import com.game.event.GameEventHandler;
import com.game.event.GameEventType;
import com.game.engine.GameEngine;
import com.game.room.Room;
import com.game.puzzle.Puzzle;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class GameStateEventHandler implements GameEventHandler {
    private final GameEngine engine;
    private final Map<GameEventType, BiConsumer<GameEvent, GameEngine>> eventHandlers;
    
    public GameStateEventHandler(GameEngine engine) {
        this.engine = engine;
        this.eventHandlers = new HashMap<>();
        initializeEventHandlers();
    }
    
    private void initializeEventHandlers() {
        eventHandlers.put(GameEventType.PLAYER_MOVED, (event, engine) -> {
            if (event.getData() instanceof Room) {
                engine.handleMovementEvent((Room) event.getData());
            }
        });
        
        eventHandlers.put(GameEventType.PUZZLE_SOLVED, (event, engine) -> {
            if (event.getData() instanceof Puzzle) {
                engine.handlePuzzleCompletion((Puzzle) event.getData());
            }
        });
        
        eventHandlers.put(GameEventType.GAME_END, (event, engine) -> 
            engine.handleGameEnd());
    }
    
    @Override
    public Set<GameEventType> getHandledEventTypes() {
        return eventHandlers.keySet();
    }
    
    @Override
    public void handleEvent(GameEvent event) {
        if (!handlesEventType(event.getType())) return;
        
        BiConsumer<GameEvent, GameEngine> handler = eventHandlers.get(event.getType());
        if (handler != null) {
            handler.accept(event, engine);
        }
    }
} 
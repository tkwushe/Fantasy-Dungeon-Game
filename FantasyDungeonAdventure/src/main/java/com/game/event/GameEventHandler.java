package com.game.event;

import java.util.Set;
import java.util.Collections;

public interface GameEventHandler {
    void handleEvent(GameEvent event);
    default Set<GameEventType> getHandledEventTypes() {
        return Collections.emptySet(); // Return empty set to handle all events, or specific set for filtered events
    }
    
    default boolean handlesEventType(GameEventType type) {
        Set<GameEventType> types = getHandledEventTypes();
        return types.isEmpty() || types.contains(type);
    }
} 
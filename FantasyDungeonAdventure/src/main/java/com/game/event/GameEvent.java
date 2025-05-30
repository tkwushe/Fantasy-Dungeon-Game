package com.game.event;

public class GameEvent {
    private final GameEventType type;
    private final Object data;

    public GameEvent(GameEventType type, Object data) {
        this.type = type;
        this.data = data;
    }



    public GameEventType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
} 
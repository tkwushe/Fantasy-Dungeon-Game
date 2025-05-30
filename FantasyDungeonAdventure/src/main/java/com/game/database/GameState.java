package com.game.database;

import com.game.player.Player;
import com.game.level.Level;

import java.io.Serial;
import java.util.List;
import java.io.Serializable;

public class GameState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Player player;
    private List<Level> levels;
    private int currentLevelIndex;
    
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    
    public List<Level> getLevels() { return levels; }
    public void setLevels(List<Level> levels) { this.levels = levels; }
    
    public int getCurrentLevelIndex() { return currentLevelIndex; }
    public void setCurrentLevelIndex(int index) { this.currentLevelIndex = index; }

}
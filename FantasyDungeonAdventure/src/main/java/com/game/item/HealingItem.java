package com.game.item;

import com.game.player.Player;
import com.game.engine.GameEngine;
import com.game.event.GameEventType;
import com.game.item.behavior.HealingBehavior;
import com.game.item.components.ItemStats;

import java.io.Serial;

public class HealingItem extends Item {
    @Serial
    private static final long serialVersionUID = 1L;
    private final boolean isFood;
    
    public HealingItem(String name, String description, int healingAmount, boolean isFood) {
        super(name, description, ItemType.HEALING, 
              new HealingBehavior(),
              new ItemStats(healingAmount * 2, healingAmount),
              true); // All healing items are consumable
        this.isFood = isFood;
    }
    
    @Override
    public boolean use(Player player) {
        boolean result = super.use(player);
        if (result) {
            String itemType = isFood ? "consume" : "use";
            GameEngine.getInstance().fireEvent(
                GameEventType.GAME_MESSAGE, 
                "You " + itemType + " the " + getName() + " and restore " + getPowerPoints() + " power points."
            );
        }
        return result;
    }

}
package com.game.item;

import com.game.player.Player;
import com.game.engine.GameEngine;
import com.game.event.GameEventType;
import com.game.item.behavior.ToolBehavior;
import com.game.item.components.ItemStats;

import java.io.Serial;

/**
 * Represents a tool or spell item that the player can use to perform actions.
 */
public class ToolItem extends Item {
    @Serial
    private static final long serialVersionUID = 1L;
    private final boolean isSpell;
    private final boolean canRevealPassages;

    public ToolItem(String name, String description, int power, boolean isSpell, boolean canRevealPassages) {
        super(name, description, ItemType.TOOL,
              new ToolBehavior(),
              createStats(power, isSpell),
              !isSpell); // Spells are consumable, tools are not
        this.isSpell = isSpell;
        this.canRevealPassages = canRevealPassages;
    }

    private static ItemStats createStats(int power, boolean isSpell) {
        ItemStats stats = new ItemStats(power * 5, power);
        if (isSpell) {
            stats.setSpellPower(power);
        } else {
            stats.setDurability(power);
        }
        return stats;
    }

    @Override
    public boolean use(Player player) {
        boolean result = super.use(player);
        try {
            if (result) {
                String message = isSpell ?
                    String.format("You cast %s with power of %d!", getName(), getSpellPower()) :
                    String.format("You use the %s. Durability: %d", getName(), getDurability());
                
                GameEngine.getInstance().fireEvent(GameEventType.GAME_MESSAGE, message);
            }
        } catch (Exception e) {
            // Ignore GameEngine errors during testing
        }
        return result;
    }

    public boolean isSpell() {
        return isSpell;
    }

    public boolean canRevealPassages() {
        return canRevealPassages;
    }

    public int getPower() {
        return isSpell ? getSpellPower() : getDurability();
    }
}

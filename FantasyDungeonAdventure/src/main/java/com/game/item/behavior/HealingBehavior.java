package com.game.item.behavior;

import com.game.player.Player;
import com.game.item.Item;

import java.io.Serial;

public class HealingBehavior implements ItemBehavior {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public boolean execute(Player player, Item item) {
        player.adjustPowerPoints(item.getPowerPoints());
        return true;  // Item is consumed after use
    }
} 
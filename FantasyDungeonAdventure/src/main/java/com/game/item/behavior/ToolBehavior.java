package com.game.item.behavior;

import com.game.player.Player;
import com.game.item.Item;
import com.game.item.ToolItem;
import com.game.event.GameEventType;
import com.game.event.GameEventDispatcher;

import java.io.Serial;

public class ToolBehavior implements ItemBehavior {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public boolean execute(Player player, Item item) {
        try {
            if (item instanceof ToolItem tool && (tool.getDurability() > 0)) {
                    tool.setDurability(tool.getDurability() - 1);
                    GameEventDispatcher.getInstance().fireEvent(
                        GameEventType.GAME_MESSAGE,
                        "You use the " + item.getName() + ". Durability: " + tool.getDurability()
                    );
                    return tool.getDurability() <= 0;

            }
        } catch (Exception e) {
            // Handle or log error
        }
        return true;
    }
} 
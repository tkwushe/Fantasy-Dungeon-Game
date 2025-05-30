package com.game.item.behavior;

import com.game.player.Player;
import com.game.item.Item;
import java.io.Serializable;

public interface ItemBehavior extends Serializable {
    boolean execute(Player player, Item item);
} 
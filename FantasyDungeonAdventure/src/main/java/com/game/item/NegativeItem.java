package com.game.item;

import com.game.player.Player;
import com.game.event.GameEventType;
import com.game.item.builder.ItemBuilder;
import com.game.item.behavior.DamageBehavior;
import com.game.event.GameEventDispatcher;

import java.io.Serial;

/**
 * Represents a negative item that causes harm to the player.
 */
public class NegativeItem extends Item {
    @Serial
    private static final long serialVersionUID = 1L;
    private boolean isDefeated;
    private final boolean isBarrier;  // Whether this is a barrier or a trap

    public NegativeItem(String name, String description, int damage) {
        this(name, description, damage, false);  // Default to trap
    }

    public NegativeItem(String name, String description, int damage, boolean isBarrier) {
        super(new ItemBuilder()
            .withName(name)
            .withDescription(description)
            .withType(ItemType.NEGATIVE)
            .withBehavior(new DamageBehavior())
            .withValue(0)
            .withPowerPoints(damage)
            .isConsumable(false)
            .build());
        this.isDefeated = false;
        this.isBarrier = isBarrier;
    }

    public void applyEffect(Player player) {
        if (!isDefeated) {
            int damage = getPowerPoints();
            if (isBarrier) {
                handleBarrierEffect(player, damage);
            } else {
                handleTrapEffect(player, damage);
            }
        }
    }

    private void handleBarrierEffect(Player player, int damage) {
        int requiredPower = damage * 2;
        int currentPower = player.getPowerPoints();
        
        // Barriers no longer deal damage, they just require power to pass
        GameEventDispatcher.getInstance().fireEvent(
            GameEventType.GAME_MESSAGE, 
            "\nA " + getName() + " blocks your path. Required power to pass: " + requiredPower
        );
        
        // Check if player has enough power to pass
        if (currentPower >= requiredPower) {
            isDefeated = true;
            GameEventDispatcher.getInstance().fireEvent(
                GameEventType.GAME_MESSAGE, 
                "Your power overwhelms the " + getName() + "! The path is now clear."
            );
        } else {
            int neededPower = requiredPower - currentPower;
            GameEventDispatcher.getInstance().fireEvent(
                GameEventType.GAME_MESSAGE, 
                "You need " + neededPower + " more power points to overcome this barrier.\n" +
                "Try finding items or solving puzzles to increase your power!"
            );
        }
    }

    private void handleTrapEffect(Player player, int damage) {
        use(player);
        isDefeated = true;  // Traps are one-time use
        GameEventDispatcher.getInstance().fireEvent(
            GameEventType.GAME_MESSAGE, 
            "\nYou triggered a " + getName() + "! It deals " + damage + " damage!"
        );
    }

    public boolean isDefeated() {
        return isDefeated;
    }

    public boolean isBarrier() {
        return isBarrier;
    }
}

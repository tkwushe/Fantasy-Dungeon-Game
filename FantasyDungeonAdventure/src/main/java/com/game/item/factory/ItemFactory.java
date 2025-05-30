package com.game.item.factory;

import com.game.item.*;
import java.util.List;

/**
 * Factory interface for creating different types of items in the game.
 */
public interface ItemFactory {
    /**
     * Creates a random item of any type.
     * @return A randomly generated item
     */
    Item createRandomItem();

    /**
     * Creates a random spell item.
     * @return A randomly generated spell item
     */
    Item createRandomSpellItem();

    /**
     * Creates a random tool item.
     * @return A randomly generated tool item
     */
    Item createRandomToolItem();

    /**
     * Creates a random healing item.
     * @return A randomly generated healing item
     */
    Item createRandomHealingItem();

    /**
     * Creates a barrier (negative item).
     * @return A barrier item
     */
    NegativeItem createBarrier();

    /**
     * Creates a random negative item (traps, curses, etc.).
     * @return A random negative item
     */
    NegativeItem createRandomNegativeItem();

    /**
     * Sets the healing rate for healing items.
     * @param rate The healing rate multiplier (0.0 to 1.0)
     */
    void setHealingItemRate(float rate);

    /**
     * Sets the strength of barrier/negative items.
     * @param strength The barrier strength multiplier
     */
    void setBarrierStrength(float strength);

    /**
     * Adds a new item name to the specified type's pool.
     * @param type The type of item
     * @param name The name to add
     */
    void addItemName(ItemType type, String name);

    /**
     * Removes an item name from the specified type's pool.
     * @param type The type of item
     * @param name The name to remove
     */
    void removeItemName(ItemType type, String name);

    /**
     * Gets all item names for a specific type.
     * @param type The type of item
     * @return List of item names
     */
    List<String> getItemNames(ItemType type);
} 
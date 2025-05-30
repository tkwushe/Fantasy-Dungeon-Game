package com.game.item;

import com.game.player.Player;
import com.game.item.interfaces.*;
import com.game.item.behavior.ItemBehavior;
import com.game.item.components.ItemStats;

import java.io.Serial;
import java.io.Serializable;

/**
 * The base class for all items in the game.
 */
public class Item implements Usable, Valuable, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String description;
    private final ItemType type;
    private final ItemBehavior behavior;
    private final ItemStats stats;
    private boolean consumable;
    private int durability;

    public Item(String name, String description, ItemType type, ItemBehavior behavior, 
                ItemStats stats, boolean consumable) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.behavior = behavior;
        this.stats = stats;
        this.consumable = consumable;
        this.durability = stats.getDurability();
    }

    // Constructor for subclasses using builder
    protected Item(Item other) {
        this.name = other.name;
        this.description = other.description;
        this.type = other.type;
        this.behavior = other.behavior;
        this.stats = other.stats;
        this.consumable = other.consumable;
        this.durability = other.durability;
    }

    @Override
    public boolean use(Player player) {
        return behavior.execute(player, this);
    }

    @Override
    public int getValue() {
        return stats.getValue();
    }

    @Override
    public void setValue(int value) {
        stats.setValue(value);
    }

    public boolean isConsumable() {
        return consumable;
    }

    public void setConsumable(boolean consumable) {
        this.consumable = consumable;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemType getType() {
        return type;
    }

    public int getPowerPoints() {
        return stats.getPowerPoints();
    }

    public void setPowerPoints(int powerPoints) {
        stats.setPowerPoints(powerPoints);
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    protected void decreaseDurability() {
        if (durability > 0) {
            durability--;
        }
    }

    protected int getSpellPower() {
        return stats.getSpellPower();
    }

    protected void setSpellPower(int spellPower) {
        stats.setSpellPower(spellPower);
    }

    public int getDamage() {
        return stats.getDamage();
    }

    protected void setDamage(int damage) {
        stats.setDamage(damage);
    }
}


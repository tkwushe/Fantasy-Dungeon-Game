package com.game.item.builder;

import com.game.item.*;
import com.game.item.behavior.ItemBehavior;
import com.game.item.components.ItemStats;

public class ItemBuilder {
    private String name;
    private String description;
    private ItemType type;
    private ItemBehavior behavior;
    private int value;
    private int powerPoints;
    private boolean consumable;

    public ItemBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ItemBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ItemBuilder withType(ItemType type) {
        this.type = type;
        return this;
    }

    public ItemBuilder withBehavior(ItemBehavior behavior) {
        this.behavior = behavior;
        return this;
    }

    public ItemBuilder withValue(int value) {
        this.value = value;
        return this;
    }

    public ItemBuilder withPowerPoints(int powerPoints) {
        this.powerPoints = powerPoints;
        return this;
    }

    public ItemBuilder isConsumable(boolean consumable) {
        this.consumable = consumable;
        return this;
    }

    public Item build() {
        ItemStats stats = new ItemStats(value, powerPoints);
        return new Item(name, description, type, behavior, stats, consumable);
    }
} 
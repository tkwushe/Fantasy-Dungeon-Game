package com.game.item.components;

import java.io.Serial;
import java.io.Serializable;

public class ItemStats implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int value;
    private int powerPoints;
    private int durability;
    private int spellPower;
    private int damage;

    public ItemStats(int value, int powerPoints) {
        this.value = value;
        this.powerPoints = powerPoints;
        this.durability = 0;
        this.spellPower = 0;
        this.damage = 0;
    }



    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getPowerPoints() {
        return powerPoints;
    }

    public void setPowerPoints(int powerPoints) {
        this.powerPoints = powerPoints;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public int getSpellPower() {
        return spellPower;
    }

    public void setSpellPower(int spellPower) {
        this.spellPower = spellPower;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
} 
package com.game.item.factory;

import com.game.item.*;
import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the ItemFactory interface.
 * Creates various types of items with random properties.
 */
public class DefaultItemFactory implements ItemFactory {
    private final Random random;
    private float healingItemRate;
    private float barrierStrength;
    
    private final Map<ItemType, List<String>> itemNames;
    
    private static final List<String> DEFAULT_HEALING_NAMES = Arrays.asList(
        "Health Potion", "Magic Elixir", "Healing Crystal", "Restoration Brew",
        "Healing Herbs", "Bandages", "Medkit", "Energy Drink"
    );
    
    private static final List<String> DEFAULT_TOOL_NAMES = Arrays.asList(
        "Torch", "Rope", "Lockpick", "Grappling Hook",
        "Fireball Scroll", "Ice Shard Wand", "Lightning Staff", "Wind Rune"
    );

    public DefaultItemFactory() {
        this.random = new Random();
        this.healingItemRate = 0.4f;  // Default values
        this.barrierStrength = 1.0f;
        
        // Initialize item name pools
        this.itemNames = new HashMap<>();
        itemNames.put(ItemType.HEALING, new ArrayList<>(DEFAULT_HEALING_NAMES));
        itemNames.put(ItemType.TOOL, new ArrayList<>(DEFAULT_TOOL_NAMES));
    }

    @Override
    public Item createRandomItem() {
        ItemType type = getRandomItemType();
        return switch (type) {
            case TOOL -> random.nextBoolean() ? createRandomToolItem() : createRandomSpellItem();
            case NEGATIVE -> createRandomNegativeItem();
            default -> createRandomHealingItem();
        };
    }

    @Override
    public Item createRandomHealingItem() {
        String name = getRandomName(ItemType.HEALING);
        int healAmount = (int)(20 * healingItemRate);
        boolean isFood = random.nextBoolean();
        return new HealingItem(name, 
            "A " + name.toLowerCase() + " that restores health", 
            healAmount,
            isFood);
    }

    @Override
    public Item createRandomSpellItem() {
        String name = getRandomName(ItemType.TOOL);
        int spellPower = random.nextInt(20) + 10;
        return new ToolItem(name,
            "A " + name.toLowerCase() + " spell",
            spellPower,
            true,  // isSpell
            false); // canRevealPassages
    }

    @Override
    public Item createRandomToolItem() {
        String name = getRandomName(ItemType.TOOL);
        int durability = random.nextInt(5) + 3;
        boolean canRevealPassages = name.equalsIgnoreCase("Torch");
        return new ToolItem(name,
            "A " + name.toLowerCase() + " that can be used multiple times",
            durability,
            false,  // isSpell
            canRevealPassages);
    }

    @Override
    public NegativeItem createBarrier() {
        int damage = (int)(15 * barrierStrength);
        return new NegativeItem("Magical Barrier", 
            "A shimmering wall of magical energy blocks your path. Power required: " + (damage * 2), 
            damage,
            true);  // isBarrier = true
    }

    @Override
    public NegativeItem createRandomNegativeItem() {
        String[] negativeItems = {
            "Poison Trap|A deadly trap that releases toxic fumes.|15",
            "Curse Rune|An ancient rune that drains your power.|18",
            "Shadow Wisp|A malevolent spirit that saps your strength.|12",
            "Thorny Vines|Sharp thorns that cause damage when touched.|10"
        };
        
        String[] chosen = negativeItems[random.nextInt(negativeItems.length)].split("\\|");
        int baseDamage = Integer.parseInt(chosen[2]);
        int scaledDamage = (int)(baseDamage * barrierStrength);
        return new NegativeItem(chosen[0], chosen[1], scaledDamage, false);  // isBarrier = false
    }

    private String getRandomName(ItemType type) {
        List<String> names = itemNames.get(type);
        if (names == null || names.isEmpty()) {
            return type.toString() + " Item";
        }
        return names.get(random.nextInt(names.size()));
    }

    private ItemType getRandomItemType() {
        return ItemType.values()[random.nextInt(ItemType.values().length)];
    }

    @Override
    public void setHealingItemRate(float rate) {
        this.healingItemRate = Math.max(0.0f, Math.min(1.0f, rate));
    }

    @Override
    public void setBarrierStrength(float strength) {
        this.barrierStrength = Math.max(0.1f, Math.min(2.0f, strength));
    }

    @Override
    public void addItemName(ItemType type, String name) {
        if (name == null || name.trim().isEmpty()) return;
        itemNames.computeIfAbsent(type, k -> new ArrayList<>()).add(name);
    }

    @Override
    public void removeItemName(ItemType type, String name) {
        List<String> names = itemNames.get(type);
        if (names != null) {
            names.remove(name);
        }
    }

    @Override
    public List<String> getItemNames(ItemType type) {
        return new ArrayList<>(itemNames.getOrDefault(type, new ArrayList<>()));
    }
} 
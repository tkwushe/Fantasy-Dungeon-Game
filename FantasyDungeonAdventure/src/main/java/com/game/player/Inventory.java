package com.game.player;

import com.game.event.GameEventDispatcher;
import com.game.event.GameEventType;
import com.game.item.Item;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Inventory implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final List<Item> items = new ArrayList<>();
    private final int maxSize;

    public Inventory(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean addItem(Item item) {
        if (item == null) return false;
        if (items.size() >= maxSize) return false;

        items.add(item);
        fireInventoryChangedEvent("Added " + item.getName());
        return true;
    }

    public boolean removeItem(Item item) {
        if (item == null) return false;
        
        boolean removed = items.remove(item);
        if (removed) {
            fireInventoryChangedEvent("Removed " + item.getName());
        }
        return removed;
    }

    private void fireInventoryChangedEvent(String action) {
        InventoryChangeInfo changeInfo = new InventoryChangeInfo(
            new ArrayList<>(items),
            action,
            items.size(),
            maxSize
        );
        GameEventDispatcher.getInstance().fireEvent(GameEventType.INVENTORY_CHANGED, changeInfo);
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public int getSize() {
        return items.size();
    }

    public boolean isFull() {
        return items.size() >= maxSize;
    }

    public Item findItemByName(String itemName) {
        if (itemName == null) return null;
        return items.stream()
                   .filter(item -> itemName.equalsIgnoreCase(item.getName()))
                   .findFirst()
                   .orElse(null);
    }

    // Inner class to hold inventory change information
    public static class InventoryChangeInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private final List<Item> currentItems;
        private final String action;
        private final int currentSize;
        private final int maxSize;

        public InventoryChangeInfo(List<Item> currentItems, String action, int currentSize, int maxSize) {
            this.currentItems = currentItems;
            this.action = action;
            this.currentSize = currentSize;
            this.maxSize = maxSize;
        }

        public List<Item> getCurrentItems() { return currentItems; }
        public String getAction() { return action; }
        public int getCurrentSize() { return currentSize; }
        public int getMaxSize() { return maxSize; }
    }
} 
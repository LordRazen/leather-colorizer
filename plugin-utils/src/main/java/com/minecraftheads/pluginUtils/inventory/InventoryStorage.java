package com.minecraftheads.pluginUtils.inventory;

import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InventoryStorage {

    private static final Set<Inventory> INVENTORIES = Collections.synchronizedSet(new HashSet<>());

    public static Set<Inventory> getInventory() {
        return INVENTORIES;
    }

    public static void addInventory(Inventory inventory) {
        INVENTORIES.add(inventory);
    }

    public static void removeInventory(Inventory inventory) {
        INVENTORIES.remove(inventory);
    }
}

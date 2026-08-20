package com.minecraftheads.pluginUtils.inventory;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryCreator {

    private static final int SIZE = 54;

    private final Inventory inventory;

    public InventoryCreator(String title) {
        this.inventory = Bukkit.createInventory(null, SIZE, title);
    }

    public void addItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    public void openInventory(Player player) {
        InventoryStorage.addInventory(inventory);
        player.openInventory(inventory);
    }
}

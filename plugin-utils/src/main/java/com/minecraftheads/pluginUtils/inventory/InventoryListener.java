package com.minecraftheads.pluginUtils.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        InventoryStorage.removeInventory(e.getInventory());
    }
}

package com.minecraftheads.leathercoloriserpro.listeners;

import com.minecraftheads.leathercoloriserpro.handlers.InventoryHandler;
import com.minecraftheads.leathercoloriserpro.handlers.SelectionHandler;
import com.minecraftheads.leathercoloriserpro.utils.InventoryCreator;
import com.minecraftheads.leathercoloriserpro.utils.ItemCreator;
import com.minecraftheads.leathercoloriserpro.utils.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class InventoryListener implements Listener {

    /**
     * Check for clicks on items
     *
     * @param e InventoryClickEvent
     */
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        // Check if inventory is a LCP inventory
        if (!InventoryHandler.getInventory().contains(e.getView().getTopInventory())) return;

        // Abort only if the InventoryClick is within the created Inventory
        if (!Objects.equals(e.getClickedInventory(), e.getView().getTopInventory())) return;

        // Item cannot be removed
        e.setCancelled(true);

        // verify current item is not null
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        // set variable player to the one who clicked
        Player player = (Player) e.getWhoClicked();

        // If item is a piece or Armor save it in the SelectionHandler
        if (Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName().startsWith("Leather")) {
            // check if player has item in inventory

            // ToDo: move into separate method
            if (player.getInventory().contains(clickedItem.getType())) {
                SelectionHandler.addItem(player, clickedItem);

                // create the inventory for choosing the color
                InventoryCreator inv = new InventoryCreator();
                inv.initializeColor();
                inv.openInventory(player);
            } else {
                player.sendMessage("You need to have the item in your inventory");
            }
            return;
            // Choose color you want to have
        } else if (Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName().startsWith("Dye")) {
            // Create a new item which can be given to the player
            ItemStack item = ItemCreator.createItem(SelectionHandler.getItem((Player) e.getWhoClicked()).getType(),
                    Objects.requireNonNull(clickedItem.getType()));

            // give item to player and remove raw item
            // ToDo: this is broken
            player.getInventory().remove(new ItemStack(SelectionHandler.getItem((Player) e.getWhoClicked()).getType(), 1));
            player.getInventory().addItem(item);

            // remove the old item in the SelectionHandler
            try {
                SelectionHandler.removeItem((Player) e.getWhoClicked());
            } catch (NullPointerException ignored) {}
            player.closeInventory();
            return;
        }
        // Player cancels the LCP -> data cleanup
        if (clickedItem.getType().equals(Material.BARRIER)) {
            e.getWhoClicked().closeInventory();
            try {
                SelectionHandler.removeItem((Player) e.getWhoClicked());
            } catch (NullPointerException ignored) {}
        }

    }

    /**
     * Cancel dragging in LCP inventory
     *
     * @param e InventoryDragEvent
     */
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (InventoryHandler.getInventory().contains(e.getView().getTopInventory())) e.setCancelled(true);
    }

    /**
     * Remove inventory from inventoryHandler when a inventory is closed
     *
     * @param e InventoryCloseEvent
     */
    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        // TODO: cleanup of old selections when player leave? could result in memoryleak if server runs a long time
        // not possible yet, as we do not map players to inventoryHandler
        if (InventoryHandler.getInventory().contains(e.getView().getTopInventory())) {
            InventoryHandler.removeInventory(e.getView().getTopInventory());
        }
    }
}
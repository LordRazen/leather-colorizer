package com.minecraftheads.leathercoloriser.handlers;

import com.minecraftheads.leathercoloriser.data.DyeColorMapping;
import com.minecraftheads.leathercoloriser.data.InventoryMapping;
import com.minecraftheads.leathercoloriser.utils.ColorChanger;
import com.minecraftheads.leathercoloriser.utils.InventoryCreator;
import com.minecraftheads.pluginUtils.utils.Logger;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;
import java.util.Random;

public class ClickHandler {
    /**
     * Handle clicks within the GUI
     *
     * @param e           InventoryClickEvent
     * @param clickedItem ItemStack
     */
    public static void handleInventoryClickGUI(InventoryClickEvent e, ItemStack clickedItem) {
        // Get actual color
        ItemStack leatherHelmet = e.getView().getTopInventory().getItem(0);
        LeatherArmorMeta meta = (LeatherArmorMeta) leatherHelmet.getItemMeta();
        Color actualColor = meta.getColor();

        // check what is clicked and perform action
        handleClick(e);
    }

    /**
     * Handle clicks within the players inventory
     *
     * @param e           InventoryClickEvent
     * @param clickedItem ItemStack
     */
    public static void handleInventoryClickInventory(InventoryClickEvent e, ItemStack clickedItem) {
        // Abort if the clicked item in the players inventory is no colorizable leather item
        Material[] leatherItems = {Material.LEATHER_HELMET,
                Material.LEATHER_CHESTPLATE,
                Material.LEATHER_LEGGINGS,
                Material.LEATHER_BOOTS,
                Material.LEATHER_HORSE_ARMOR};
        if (!Arrays.asList(leatherItems).contains(clickedItem.getType())) return;

        // Get Player
        Player player = (Player) e.getWhoClicked();

        // Rip color from leather item
        LeatherArmorMeta meta = (LeatherArmorMeta) clickedItem.getItemMeta();
        SelectionHandler.setColor(player, meta.getColor());

        // Open new INV with ripped color
        new InventoryCreator(player).openInventory(player);
    }


    /**
     * check what item is clicked and perform action depending on that
     *
     * @param e InventoryClickEvent
     */
    private static void handleClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        Color col = ((LeatherArmorMeta) e.getInventory().getItem(0).getItemMeta()).getColor();


        try {
            InventoryMapping im = InventoryMapping.getBySlot(e.getSlot());
            switch (im.getAction()) {
                // generate random color
                case ("randomColor"):
                    Random obj = new Random();
                    SelectionHandler.setColor(player, Color.fromRGB(obj.nextInt(0xffffff + 1)));
                    new InventoryCreator(player);
                    break;
                // Close inventory and send message to player how to use custom color codes
                case ("hexCommand"):
                    player.closeInventory();
                    player.sendMessage(LanguageHandler.getMessage("error_invalid_color"));
                    break;
                case ("decrease_hue"):
                    ColorChanger.DECREASE_HUE.apply(player);
                    new InventoryCreator(player);
                    break;
                case ("increase_hue"):
                    ColorChanger.INCREASE_HUE.apply(player);
                    new InventoryCreator(player);
                    break;
                case ("decrease_saturation"):
                    ColorChanger.INCREASE_SATURATION.apply(player);
                    new InventoryCreator(player);
                    break;
                case ("increase_saturation"):
                    ColorChanger.DECREASE_SATURATION.apply(player);
                    new InventoryCreator(player);
                    break;
                case ("decrease_brightness"):
                    ColorChanger.DECREASE_BRIGHTNESS.apply(player);
                    new InventoryCreator(player);
                    break;
                case ("increase_brightness"):
                    ColorChanger.INCREASE_BRIGHTNESS.apply(player);
                    new InventoryCreator(player);
                    break;
                case ("reset"):
                    SelectionHandler.setColor(player, DyeColorMapping.DEFAULT.getColor());
                    new InventoryCreator(player);
                    break;
                case ("dye"):
                    // Mix Colors if the color is not the default one
                    Color newColor;
                    try {
                        newColor = DyeColorMapping.getColorByMat(im.getMaterial());
                    } catch (NullPointerException ignored) {
                        newColor = DyeColorMapping.DEFAULT.getColor();
                    }
                    SelectionHandler.setColor(player, newColor);
                    if (!col.equals(DyeColorMapping.DEFAULT.getColor())) {
                        SelectionHandler.setColor(player, newColor.mixColors(col));
                    }
                    // Create the inventory for choosing the color
                    new InventoryCreator(player);
                    break;
                case ("armor"):
                    // ToDo move this into method
                    // Clean Armor
                    if (col.equals(DyeColorMapping.DEFAULT.getColor())) {
                        ItemStack[] items = player.getInventory().getContents();
                        for (int i = 0; i < items.length; i++) {
                            if (items[i] != null && items[i].getType() == clickedItem.getType()) {
                                LeatherArmorMeta meta = (LeatherArmorMeta) items[i].getItemMeta();
                                if (!meta.getColor().equals(DyeColorMapping.DEFAULT.getColor())) {
                                    player.getInventory().clear(i);
                                    player.getInventory().addItem(new ItemStack(clickedItem.getType(), 1));
                                    break;
                                }
                            }
                        }

                    }

                    // Colorise Armor
                    else {
                        if (checkRequirement(player, clickedItem)) {
                            // Search for the first item in the inventory of the player which is the base item of the colored one
                            player.getInventory().clear(player.getInventory().first(new ItemStack(clickedItem.getType(), 1)));
                            player.getInventory().addItem(clickedItem);
                        }
                    }
                    break;
            }

        } catch (NullPointerException ignored) {

        }

        // Open Watermark
        if (clickedItem.getType().equals(Material.PUFFERFISH)) {
            player.closeInventory();
            player.sendMessage("§6[§4LC§6] §aGet more plugins for detailed decoration at\n§3www.minecraft-heads.com");
        }


    }

    /**
     * Check if the player has the needed item in his inventory
     *
     * @param player Player
     * @param item   ItemStack
     * @return boolean
     */
    private static boolean checkRequirement(Player player, ItemStack item) {
        if (player.getInventory().contains(new ItemStack(item.getType(), 1)))
            return true;

        player.sendMessage(LanguageHandler.getMessage("error_item_missing"));
        return false;
    }

    /**
     * Generate colored armor piece, add it to inventory and remove raw (uncolored) armor
     *
     * @param player Player
     * @param item   ItemStack
     */
    private static void giveItem(Player player, ItemStack item) {
        // Search for the first item in the inventory of the player which is the base item of the colored one
        player.getInventory().clear(player.getInventory().first(new ItemStack(item.getType(), 1)));
        player.getInventory().addItem(item);

        // Remove the color in the SelectionHandler
        try {
            SelectionHandler.removeColor(player);
        } catch (NullPointerException ignored) {
        }
    }
}

package com.minecraftheads.leathercoloriserpro.commands;

import com.minecraftheads.leathercoloriserpro.handlers.LanguageHandler;
import com.minecraftheads.leathercoloriserpro.utils.InventoryCreator;
import com.minecraftheads.leathercoloriserpro.utils.Logger;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLeatherColoriserPro implements CommandExecutor {

    /**
     * Initiates LCP when /lcp is entered
     *
     * @param sender CommandSender
     * @param command Command
     * @param label String
     * @param args String
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            // Default
            if (args.length == 0) {
                InventoryCreator inv = new InventoryCreator();
                inv.initializeUncoloredArmor();
                inv.openInventory(player);
            }
            // Check if user provided an argument
            else if (args.length == 1) {
                // check by regex if string matches HEX code
                String regex = "^[#]?[0-9a-fA-F]{6}$";
                if (args[0].contains("#")) {
                    args[0] = args[0].replace("#", "");
                }
                if (args[0].matches(regex)) {
                    InventoryCreator inv = new InventoryCreator();
                    inv.initializeColoredArmor(Color.fromRGB(Integer.parseInt(args[0], 16)));
                    inv.openInventory(player);
                }
                // invalid argument
                else {
                    player.sendMessage(LanguageHandler.getMessage("error_invalid_color"));
                }
            } else {
                return false;
            }

        } else {
            Logger.info(LanguageHandler.getMessage("error_invalid_command_sender"));
        }
        return true;
    }

}

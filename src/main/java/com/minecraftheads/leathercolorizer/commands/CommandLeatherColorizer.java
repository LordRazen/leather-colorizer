package com.minecraftheads.leathercolorizer.commands;

import com.minecraftheads.leathercolorizer.data.DyeColorMapping;
import com.minecraftheads.leathercolorizer.data.LanguageMapping;
import com.minecraftheads.leathercolorizer.handlers.SelectionHandler;
import com.minecraftheads.leathercolorizer.utils.InventoryCreatorBridge;
import com.minecraftheads.pluginUtils.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CommandLeatherColorizer implements TabExecutor {

    private final List<String> tabConsole = Arrays.asList("version", "reload");
    private final List<String> tabAdmin = Arrays.asList("info", "#FFFFFF", "version", "reload");
    private final List<String> tabPlayer = Arrays.asList("info", "#FFFFFF");


    /**
     * Initiates Leather Colorizer when /lc is entered
     *
     * @param sender  CommandSender
     * @param command Command
     * @param label   String
     * @param args    String
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            return onCommandPlayer(sender, command, label, args);
        } else {
            return onCommandConsole(sender, command, label, args);
        }
    }

    /**
     * Commands used by players
     *
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    public boolean onCommandPlayer(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        // Check basic permission
        if (!player.hasPermission("leathercolorizer.main")) {
            player.sendMessage(LanguageMapping.ERROR_PERMISSION_MISSING.getStringWithPrefix());
            return true;
        }

        // Zero Arguments => Open Clean LC
        if (args.length == 0) {
            if (SelectionHandler.getColor(player) == null) {
                SelectionHandler.setColor(player, DyeColorMapping.DEFAULT.getColor());
            }

            new InventoryCreatorBridge(player);
        }

        // One Argument
        else if (args.length == 1) {

            // Reload command
            if (args[0].equals("reload")) {
                if (player.hasPermission("leathercolorizer.admin")) {
                    player.sendMessage(LanguageMapping.RELOAD.getStringWithPrefix());
                    // TODO: RELOAD
                } else {
                    player.sendMessage(LanguageMapping.ERROR_PERMISSION_MISSING.getStringWithPrefix());
                }
                return true;
            }

            // Version command
            if (args[0].equals("version")) {
                if (player.hasPermission("leathercolorizer.admin")) {
                    player.sendMessage(LanguageMapping.TITLE_CHAT.getStringWithPrefix() + " v" +
                            Bukkit.getPluginManager().getPlugin("LeatherColorizer").getDescription().getVersion());
                } else {
                    player.sendMessage(LanguageMapping.ERROR_PERMISSION_MISSING.getStringWithPrefix());
                }
                return true;
            }

            // Info command
            if (args[0].equals("info")) {
                String info = LanguageMapping.TITLE_CHAT.getStringWithPrefix() + "\n" +
                        LanguageMapping.INFO_OPEN_LC.getString() + "\n" +
                        LanguageMapping.INFO_OPEN_LC_RGB.getString() + "\n" +
                        LanguageMapping.INFO_INFO.getString() + "\n";
                // Additional infos for Admins
                if (player.hasPermission("leathercolorizer.admin")) {
                    info += LanguageMapping.INFO_RELOAD.getString() + "\n" +
                            LanguageMapping.INFO_VERSION.getString() + "\n";
                }
                player.sendMessage(info);
                return true;
            }

            // Color Command => Check by regex if string matches HEX Color Code
            String regex = "^[#]?[0-9a-fA-F]{6}$";
            if (args[0].contains("#")) {
                args[0] = args[0].replace("#", "");
            }
            if (args[0].matches(regex)) {
                SelectionHandler.setColor(player, Color.fromRGB(Integer.parseInt(args[0], 16)));
                new InventoryCreatorBridge(player);
            }
            // Invalid Color
            else {
                player.sendMessage(LanguageMapping.ERROR_INVALID_COLOR.getStringWithPrefix());
            }
        }

        // More arguments => Invalid
        else {
            player.sendMessage(LanguageMapping.ERROR_INVALID_COLOR.getStringWithPrefix());
        }
        return true;
    }

    /**
     * Commands on Console
     *
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    public boolean onCommandConsole(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // Reload command
            if (args[0].equals("reload")) {
                Logger.info("Leather Colorizer reloaded");
                // TODO: RELOAD
                return true;
            }

            // Version command
            if (args[0].equals("version")) {
                Logger.info("Leather Colorizer v" +
                        Bukkit.getPluginManager().getPlugin("LeatherColorizer").getDescription().getVersion());
                return true;
            }
        }

        Logger.info("Invalid command");
        return true;
    }

    /**
     * Auto Complete for Leather Colorizer commands
     *
     * @param sender  CommandSender
     * @param command Command
     * @param alias   String
     * @param args    String
     * @return
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Player
        if (sender instanceof Player) {
            if (sender.hasPermission("leathercolorizer.admin")) {
                return tabAdmin;
            } else if (sender.hasPermission("leathercolorizer.main")) {
                return tabPlayer;
            }
        }
        // Console
        else {
            return tabConsole;
        }
        return null;
    }
}

package com.minecraftheads.pluginUtils.utils;

import java.util.logging.Level;

public class Logger {

    private static String prefix = "";
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("Minecraft");

    public static void setPrefix(String prefix) {
        Logger.prefix = prefix;
    }

    public static void info(String message) {
        LOGGER.log(Level.INFO, prefix + " " + message);
    }
}

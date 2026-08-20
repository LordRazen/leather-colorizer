package com.minecraftheads.pluginUtils.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class LanguageHandler {

    private static JavaPlugin plugin;
    private static YamlConfiguration languageConfig;
    private static String loadedLanguage;

    public static void setPlugin(JavaPlugin javaPlugin) {
        plugin = javaPlugin;
        languageConfig = null;
        loadedLanguage = null;
    }

    public static String getString(String path) {
        ensureLoaded();
        return languageConfig == null ? path : languageConfig.getString(path, path);
    }

    /**
     * The language file may not exist yet the first time this is called (LanguageHandler.setPlugin
     * runs before the plugin has copied its language resources to disk), and the configured
     * language can also change between calls, so reload whenever the target language differs from
     * what's currently cached.
     */
    private static void ensureLoaded() {
        if (plugin == null) return;

        String language = plugin.getConfig().getString("language", "en");
        if (languageConfig != null && language.equals(loadedLanguage)) return;

        File languageFile = new File(plugin.getDataFolder(), "languages/" + language + ".yml");
        if (!languageFile.exists()) return;

        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
        loadedLanguage = language;
    }
}

package com.minecraftheads.pluginUtils.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Merges the plugin's bundled default resource (e.g. config.yml, a languages/*.yml) into the copy
 * already on disk: keys the user already has keep their value, keys new to the template get added
 * (with the template's comments), and keys the user added that aren't in the template are kept as
 * well. Only handles flat, single-level YAML (no nested maps/lists), which is all this plugin's
 * config and language files use.
 */
public class ConfigUpdater {

    private static final Pattern KEY_LINE = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*):.*$");

    public static void update(JavaPlugin plugin, String resourcePath, File toUpdate, List<String> ignoredSections) throws IOException {
        Set<String> ignored = new LinkedHashSet<>(ignoredSections);

        List<String> templateLines = readTemplateLines(plugin, resourcePath);
        YamlConfiguration templateConfig = loadTemplateConfig(plugin, resourcePath);
        YamlConfiguration existingConfig = toUpdate.exists() ? YamlConfiguration.loadConfiguration(toUpdate) : new YamlConfiguration();

        List<String> output = new ArrayList<>();
        Set<String> writtenKeys = new LinkedHashSet<>();
        List<String> commentBuffer = new ArrayList<>();

        for (String line : templateLines) {
            Matcher matcher = KEY_LINE.matcher(line);
            if (!matcher.matches()) {
                commentBuffer.add(line);
                continue;
            }

            String key = matcher.group(1);
            if (ignored.contains(key)) {
                commentBuffer.clear();
                continue;
            }

            output.addAll(commentBuffer);
            commentBuffer.clear();

            Object value = existingConfig.contains(key) ? existingConfig.get(key) : templateConfig.get(key);
            output.add(formatKeyValue(key, value));
            writtenKeys.add(key);
        }

        // Keys the user has that aren't in the template (custom additions, or ignored sections) are
        // preserved by appending them, rather than being dropped.
        for (String key : existingConfig.getKeys(false)) {
            if (writtenKeys.contains(key)) continue;
            output.add(formatKeyValue(key, existingConfig.get(key)));
        }

        Files.write(toUpdate.toPath(), output, StandardCharsets.UTF_8);
    }

    private static List<String> readTemplateLines(JavaPlugin plugin, String resourcePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                throw new IOException("Bundled resource not found: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private static YamlConfiguration loadTemplateConfig(JavaPlugin plugin, String resourcePath) throws IOException {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                throw new IOException("Bundled resource not found: " + resourcePath);
            }
            return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    private static String formatKeyValue(String key, Object value) {
        YamlConfiguration single = new YamlConfiguration();
        single.set(key, value);
        return single.saveToString().trim();
    }
}

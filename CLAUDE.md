# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Leather Colorizer is a Spigot/Bukkit Minecraft server plugin (Java 21, Minecraft 1.20.6+) that lets
players recolor leather armor, leather horse armor, and wolf armor via a chest-GUI or `/lc <hex>`.

## Build

```
cd plugin-utils && mvn install
cd .. && mvn package
```

`plugin-utils/` (in this repo) is a clean-room reimplementation of `com.minecraftheads:plugin-utils`.
The original library's source repo and published artifact are both gone and its author is
unreachable, so it was rebuilt from scratch against exactly the API surface `leather-colorizer` calls
into — see `plugin-utils/pom.xml`'s `<description>`. It must be `mvn install`ed to the local Maven
repository before `mvn package` at the repo root, since the root `pom.xml` depends on it as
`com.minecraftheads:plugin-utils:1.8-SNAPSHOT`. There are no automated tests in either project.

`maven-shade-plugin` produces the runnable jar (with `plugin-utils` shaded in); an `antrun` execution
then copies it to `latest/Leather-Colorizer-latest.jar`. Resources under `src/main/resources` are
filtered, so `${project.version}` in `plugin.yml` is resolved from `pom.xml` at build time.

## Architecture

The plugin's own code is a thin layer on top of `com.minecraftheads.pluginUtils` (`plugin-utils/` in
this repo — see Build above) which provides the GUI framework (`InventoryCreator`, `InventoryStorage`,
and an `InventoryListener` that evicts closed inventories from `InventoryStorage`), i18n
(`LanguageHandler`), config migration (`ConfigUpdater`), custom-head textures (`HeadCreator`), and
logging (`Logger`). Its API surface is intentionally minimal — only what `leather-colorizer` actually
calls — so don't assume it has functionality beyond what's used here.

Flow for a typical color change:

1. `LeatherColorizer` (main class) registers `CommandLeatherColorizer` and the two listeners on `onEnable`.
2. `CommandLeatherColorizer` handles `/lc`, `/lc <hex>`, `/lc info|version|reload`, storing the chosen
   color via `SelectionHandler` (an in-memory `Player -> Color` map — the source of truth for "what
   color is currently being edited") and opening the GUI through `InventoryCreatorBridge`.
3. `InventoryCreatorBridge` builds the GUI inventory from `InventoryMapping`, an enum where each value
   is one clickable slot (armor preview, dye, hue/saturation/brightness head, reset, random, hex-prompt)
   carrying its slot index, `Material`, display name, and an `action` string tag.
4. `InventoryListener` intercepts clicks/drags in any plugin-utils-tracked inventory and cancels them
   (nothing can be picked up), then routes to `ClickHandler`.
5. `ClickHandler.handleClick` switches on the clicked slot's `action` tag (`"dye"`, `"randomColor"`,
   `"increase_hue"`, `"armor"`, etc.), mutates the color in `SelectionHandler` via `ColorChanger`
   (HSB math on the current `Color`) or `DyeColorMapping` (dye-to-`Color` + color mixing), then
   re-renders by opening a fresh `InventoryCreatorBridge`. Clicking an armor slot instead colorizes
   or cleans the player's matching leather items in-place, gated by the `allowDamagedItems` /
   `allowEnchantedItems` / `keepEnchantments` / `keepTitle` / `keepLore` config flags.
6. `PlayerListener` evicts the player's entry from `SelectionHandler` on quit to avoid a memory leak.

Custom GUI icons (hue/saturation/brightness arrows, the random-color head) are `textures.minecraft.net`
skin URLs defined in `CustomHeads` and resolved to `ItemStack`s via `plugin-utils`' `HeadCreator`.

All user-facing strings are looked up through `LanguageMapping` (an enum of message keys) →
`LanguageHandler`, backed by the YAML files in `src/main/resources/languages/` (`en`, `de`, `it`, `ru`,
`tr`). When adding a new user-facing string, add the key to every language file, not just `en.yml`,
and add a matching constant to `LanguageMapping`.

`config.yml` is versioned and updated on startup via `ConfigUpdater` (`LeatherColorizer.checkConfig`),
which also seeds missing per-language files into the plugin's data folder on first run.

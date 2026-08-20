package com.minecraftheads.pluginUtils.heads;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class HeadCreator {

    public static ItemStack getHead(String textureUrl) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(new URL(textureUrl));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid head texture URL: " + textureUrl, e);
        }
        profile.setTextures(textures);

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);

        return head;
    }
}

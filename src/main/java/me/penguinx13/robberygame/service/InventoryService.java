package me.penguinx13.robberygame.service;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public final class InventoryService {

    private static final String SAVED_INVENTORY_KEY = "robberyGameSavedInventory";

    private final JavaPlugin plugin;

    public InventoryService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void saveAndClear(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] savedInventory = playerInventory.getContents();
        playerInventory.clear();
        player.setMetadata(SAVED_INVENTORY_KEY, new FixedMetadataValue(plugin, savedInventory));
    }

    public void giveRobberyTool(Player player, Material targetMaterial) {
        String command = "minecraft:give " + player.getName() + " iron_pickaxe 1 0 {CanDestroy:["
                + targetMaterial.getKey().getKey() + "]}";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void restore(Player player) {
        if (!player.hasMetadata(SAVED_INVENTORY_KEY)) {
            return;
        }

        List<MetadataValue> metadata = player.getMetadata(SAVED_INVENTORY_KEY);
        if (metadata.isEmpty() || !(metadata.get(0).value() instanceof ItemStack[] savedInventory)) {
            return;
        }

        PlayerInventory playerInventory = player.getInventory();
        playerInventory.clear();
        playerInventory.setContents(savedInventory);
        player.removeMetadata(SAVED_INVENTORY_KEY, plugin);
    }
}

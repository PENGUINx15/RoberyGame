package me.penguinx13.robberygame;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class RobberyGame extends JavaPlugin {

    private BlockBreakListener  blockBreakListener ;

    private void registerListeners() {
        blockBreakListener = new BlockBreakListener(this);
        Bukkit.getPluginManager().registerEvents(blockBreakListener, this);
    }

    @Override
    public void onEnable() {

        saveDefaultConfig();
        registerListeners();

        getLogger().info("RobberyGamePlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RobberyGamePlugin has been disabled!");
    }

    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("robberygame")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("start")) {
                if (sender instanceof Player player) {
                    if (!isLocationOccupied()) {
                        blockBreakListener.time = 25;
                        player.setGameMode(GameMode.ADVENTURE);


                        blockBreakListener.startTimer(player);
                        saveAndClearInventory(player);

                        giveIronPickaxe(player);

                        player.teleport(blockBreakListener.startLocation);

                        sender.sendMessage("Ограбление началось!");
                    } else {
                        sender.sendMessage("Подождите, сейчас идет ограбление!");
                    }
                } else {
                    sender.sendMessage("Эту команду могут использовать только игроки.");
                }
            }
            return true;
        }
        return false;
    }


    public void saveAndClearInventory(Player player) {
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] savedInventory = playerInventory.getContents();
        playerInventory.clear();

        player.setMetadata("savedInventory", new FixedMetadataValue(this, savedInventory));
    }
    public void giveIronPickaxe(Player player) {
        String command = "minecraft:give " + player.getName() + " iron_pickaxe 1 0 {CanDestroy:[diamond_block]}";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
    public void restoreInventory(Player player) {
        if (player.hasMetadata("savedInventory")) {
            PlayerInventory playerInventory = player.getInventory();

            // Очищення поточного інвентаря
            playerInventory.clear();

            // Отримання збереженого інвентаря з метаданих гравця
            List<MetadataValue> metadata = player.getMetadata("savedInventory");
            if (!metadata.isEmpty()) {
                ItemStack[] savedInventory = (ItemStack[]) metadata.get(0).value();

                assert savedInventory != null;
                playerInventory.setContents(savedInventory);

                player.removeMetadata("savedInventory", this);
            }
        }
    }





    public boolean isPlayerInRobberyLocation(Player player) {
        Location playerLocation = player.getLocation();
        return blockBreakListener.isLocationInRange(playerLocation, blockBreakListener.minLocation, blockBreakListener.maxLocation);
    }

    private boolean isLocationOccupied() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (isPlayerInRobberyLocation(onlinePlayer)) {
                return true;
            }
        }
        return false;
    }
}
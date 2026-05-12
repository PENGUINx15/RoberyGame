package me.penguinx13.robberygame;

import me.penguinx13.robberygame.command.RobberyCommand;
import me.penguinx13.robberygame.command.WapiCommandRegistry;
import me.penguinx13.robberygame.config.RobberySettings;
import me.penguinx13.robberygame.listener.BlockBreakListener;
import me.penguinx13.robberygame.service.BlockSpawner;
import me.penguinx13.robberygame.service.InventoryService;
import me.penguinx13.robberygame.service.RewardService;
import me.penguinx13.robberygame.service.RobberyService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class RobberyGame extends JavaPlugin {

    private RobberyService robberyService;
    private WapiCommandRegistry commandRegistry;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        RobberySettings settings = RobberySettings.load(this);
        InventoryService inventoryService = new InventoryService(this);
        RewardService rewardService = new RewardService(this);
        BlockSpawner blockSpawner = new BlockSpawner(settings);
        robberyService = new RobberyService(this, settings, inventoryService, rewardService, blockSpawner);

        registerListeners();
        registerCommands();

        getLogger().info("RobberyGamePlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (robberyService != null) {
            robberyService.shutdown();
        }
        if (commandRegistry != null) {
            commandRegistry.shutdown();
        }
        getLogger().info("RobberyGamePlugin has been disabled!");
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(robberyService), this);
    }

    private void registerCommands() {
        commandRegistry = new WapiCommandRegistry(this);
        commandRegistry.register(new RobberyCommand(robberyService));
        commandRegistry.bind();
    }
}

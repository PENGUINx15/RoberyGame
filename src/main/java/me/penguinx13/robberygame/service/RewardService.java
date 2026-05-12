package me.penguinx13.robberygame.service;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class RewardService {

    private final JavaPlugin plugin;

    public RewardService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int randomAmount(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public boolean deposit(Player player, int amount) {
        Optional<Economy> economy = economy();
        economy.ifPresent(value -> value.depositPlayer(player, amount));
        return economy.isPresent();
    }

    private Optional<Economy> economy() {
        RegisteredServiceProvider<Economy> registration = plugin.getServer()
                .getServicesManager()
                .getRegistration(Economy.class);
        if (registration == null) {
            plugin.getLogger().warning("Vault Economy provider is not available.");
            return Optional.empty();
        }
        return Optional.ofNullable(registration.getProvider());
    }
}

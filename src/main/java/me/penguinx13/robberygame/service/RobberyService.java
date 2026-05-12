package me.penguinx13.robberygame.service;

import java.util.Optional;
import java.util.UUID;

import me.penguinx13.robberygame.config.RobberySettings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class RobberyService {

    private final JavaPlugin plugin;
    private final RobberySettings settings;
    private final InventoryService inventoryService;
    private final RewardService rewardService;
    private final BlockSpawner blockSpawner;

    private RobberySession activeSession;
    private UUID activePlayerId;

    public RobberyService(
            JavaPlugin plugin,
            RobberySettings settings,
            InventoryService inventoryService,
            RewardService rewardService,
            BlockSpawner blockSpawner
    ) {
        this.plugin = plugin;
        this.settings = settings;
        this.inventoryService = inventoryService;
        this.rewardService = rewardService;
        this.blockSpawner = blockSpawner;
    }

    public boolean start(Player player) {
        if (isOccupied()) {
            return false;
        }

        activePlayerId = player.getUniqueId();
        activeSession = new RobberySession(plugin, player, settings.durationSeconds(), () -> timeout(player));

        player.setGameMode(GameMode.ADVENTURE);
        inventoryService.saveAndClear(player);
        inventoryService.giveRobberyTool(player, settings.blockMaterial());
        player.teleport(settings.startLocation());
        blockSpawner.spawnRobberyBlocks();
        activeSession.startTimer();
        return true;
    }

    public void handleRobberyBlockBreak(Player player) {
        Optional<RobberySession> session = sessionFor(player);
        if (session.isEmpty()) {
            return;
        }

        int brokenBlocks = session.get().incrementBrokenBlocks();
        player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent("§fВы сломали §6" + settings.blockMaterial().name() + "§f. Всего сломаных блоков:§6 " + brokenBlocks)
        );

        if (brokenBlocks >= settings.requiredBlocks()) {
            complete(player);
        }
    }

    public boolean isRobberyBlock(Location location) {
        return settings.robberyRegion().contains(location)
                && location.getBlock().getType() == settings.blockMaterial();
    }

    public boolean isPlayerInRobberyRegion(Player player) {
        return settings.robberyRegion().contains(player.getLocation());
    }

    public boolean isOccupied() {
        if (activeSession != null) {
            return true;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPlayerInRobberyRegion(player)) {
                return true;
            }
        }
        return false;
    }

    public void shutdown() {
        if (activeSession != null) {
            activeSession.finish();
        }
        activeSession = null;
        activePlayerId = null;
    }

    private Optional<RobberySession> sessionFor(Player player) {
        if (activeSession == null || activePlayerId == null || !activePlayerId.equals(player.getUniqueId())) {
            return Optional.empty();
        }
        return Optional.of(activeSession);
    }

    private void complete(Player player) {
        finishSession(player);
        player.teleport(settings.winLocation());
        player.sendMessage("§fВы сломали§6 " + settings.requiredBlocks() + " §fблоков. Ограбление завершено.");

        int amount = rewardService.randomAmount(settings.currencyMin(), settings.currencyMax());
        if (rewardService.deposit(player, amount)) {
            player.sendMessage("§fВы ограбили банк на суму §6" + amount + " $.");
        } else {
            player.sendMessage("§cНаграда не выдана: экономика Vault недоступна.");
        }
    }

    private void timeout(Player player) {
        finishSession(player);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "jail " + player.getName() + " 5m");
        player.teleport(settings.loseLocation());
        player.sendMessage("§cВремя вышло! Ограбление завершено.");
    }

    private void finishSession(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        inventoryService.restore(player);
        if (activeSession != null) {
            activeSession.finish();
        }
        activeSession = null;
        activePlayerId = null;
    }
}

package me.penguinx13.robberygame;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;


public class BlockBreakListener implements Listener {

    private final RobberyGame plugin;


    public Location minLocation;
    public Location maxLocation;
    private BossBar bossBar;
    public Location startLocation;
    public Location stopLocation;
    public Location loseLocation;
    private int brokenBlocksCount;
    private BukkitRunnable timer;
    public int time = 60;

    private final int getCurrencyMin;
    private final int getCurrencyMax;

    public BlockBreakListener(RobberyGame plugin) {
        this.plugin = plugin;

        String worldName = plugin.getConfig().getString("RobberyLocation.world");
        int minX = plugin.getConfig().getInt("RobberyLocation.minX");
        int minY = plugin.getConfig().getInt("RobberyLocation.minY");
        int minZ = plugin.getConfig().getInt("RobberyLocation.minZ");
        int maxX = plugin.getConfig().getInt("RobberyLocation.maxX");
        int maxY = plugin.getConfig().getInt("RobberyLocation.maxY");
        int maxZ = plugin.getConfig().getInt("RobberyLocation.maxZ");
        assert worldName != null;
        minLocation = new Location(plugin.getServer().getWorld(worldName), minX, minY, minZ);
        maxLocation = new Location(plugin.getServer().getWorld(worldName), maxX, maxY, maxZ);

        String startWorldName = plugin.getConfig().getString("RobberyTPLocation.Start.world");
        int startX = plugin.getConfig().getInt("RobberyTPLocation.Start.X");
        int startY = plugin.getConfig().getInt("RobberyTPLocation.Start.Y");
        int startZ = plugin.getConfig().getInt("RobberyTPLocation.Start.Z");
        assert startWorldName != null;
        startLocation = new Location(plugin.getServer().getWorld(startWorldName), startX, startY, startZ);

        String winWorldName = plugin.getConfig().getString("RobberyTPLocation.Win.world");
        int winX = plugin.getConfig().getInt("RobberyTPLocation.Win.X");
        int winY = plugin.getConfig().getInt("RobberyTPLocation.Win.Y");
        int winZ = plugin.getConfig().getInt("RobberyTPLocation.Win.Z");
        assert winWorldName != null;
        stopLocation = new Location(plugin.getServer().getWorld(winWorldName), winX, winY, winZ);

        String loseWorldName = plugin.getConfig().getString("RobberyTPLocation.Lose.world");
        int loseX = plugin.getConfig().getInt("RobberyTPLocation.Lose.X");
        int loseY = plugin.getConfig().getInt("RobberyTPLocation.Lose.Y");
        int loseZ = plugin.getConfig().getInt("RobberyTPLocation.Lose.Z");
        assert loseWorldName != null;
        loseLocation = new Location(plugin.getServer().getWorld(loseWorldName), loseX, loseY, loseZ);

        getCurrencyMin = plugin.getConfig().getInt("CurrencyMin");
        getCurrencyMax = plugin.getConfig().getInt("CurrencyMax");

        brokenBlocksCount = 0;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Location blockLocation = event.getBlock().getLocation();

        if (isLocationInRange(blockLocation, minLocation, maxLocation)) {
            if (event.getBlock().getType() == Material.DIAMOND_BLOCK) {
                brokenBlocksCount++;
                updateBossBar(player);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§fВы сломали §6алмазный блок§f. Всего сломаных блоков:§6 " + brokenBlocksCount));
                if (brokenBlocksCount >= 8) {
                    resetCounter(player);
                    clearBossBar(player);

                    player.teleport(stopLocation);
                    brokenBlocksCount = 0;
                    player.sendMessage("§fВы сломали§6 8 §fблоков. Ограбление завершено.");
                    int min = getCurrencyMin;
                    int amount = new Random().nextInt(getCurrencyMax - min + 1) + min;

                    plugin.restoreInventory(player);

                    Economy economy = plugin.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
                    economy.depositPlayer(player, amount);
                    player.sendMessage("§fВы ограбили банк на суму §6" + amount + " $.");

                }
            }
        }
    }

    public boolean isLocationInRange(Location location, Location minLocation, Location maxLocation) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= minLocation.getX() && x <= maxLocation.getX()
                && y >= minLocation.getY() && y <= maxLocation.getY()
                && z >= minLocation.getZ() && z <= maxLocation.getZ();
    }

    public void resetCounter(Player player) {
        brokenBlocksCount = 0;
        player.setGameMode(GameMode.SURVIVAL);
        if (timer != null) {
            timer.cancel();
            timer = null;

        }
    }

    public void startTimer(Player player) {
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (time > 0) {
                    time--;
                    updateBossBar(player);
                } else {
                    resetCounter(player);
                    clearBossBar(player);
                    String command = "jail " + player.getName() + " 5m";
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    plugin.restoreInventory(player);
                    player.sendMessage("§cВремя вышло! Ограбление завершено.");
                }
            }
        };
        timer.runTaskTimer(plugin, 0L, 20L); // Викликати кожну секунду (20 тіків)
        generateDiamondBlocks();
    }

    private void generateDiamondBlocks() {
        World world = minLocation.getWorld();

        ConfigurationSection spawnLocations = plugin.getConfig().getConfigurationSection("Blocks.LocationSpawn");
        if (spawnLocations == null) {
            plugin.getLogger().warning("§cНеверный формат LocationSpawn!");
            return;
        }

        for (String key : spawnLocations.getKeys(false)) {
            ConfigurationSection locationSection = spawnLocations.getConfigurationSection(key);
            if (locationSection == null) {
                continue;
            }

            int x = locationSection.getInt("X");
            int y = locationSection.getInt("Y");
            int z = locationSection.getInt("Z");

            Location location = new Location(world, x, y, z);
            Block block = location.getBlock();


            new Random().nextDouble();
            block.setType(Material.DIAMOND_BLOCK);

        }
    }

    private void updateBossBar(Player player) {
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar("§fВремя: §6 25 сек. §0|§f Сломано блоков: §60", BarColor.GREEN, BarStyle.SOLID);
            bossBar.addPlayer(player);
        }

        String bossBarText = "§fВремя: §6" + time + " сек. §0|§f Сломано блоков: §6" + brokenBlocksCount;
        bossBar.setTitle(bossBarText);
        bossBar.setProgress((double) time / 25); // Прогрес від 0.0 до 1.0



    }

    private void clearBossBar(Player player) {
        bossBar.removePlayer(player);
        bossBar.setVisible(false);
        bossBar.setProgress(0.0);
        bossBar = null;
    }
}
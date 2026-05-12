package me.penguinx13.robberygame.config;

import java.util.ArrayList;
import java.util.List;

import me.penguinx13.robberygame.model.CuboidRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class RobberySettings {

    private final CuboidRegion robberyRegion;
    private final Location startLocation;
    private final Location winLocation;
    private final Location loseLocation;
    private final List<Location> blockSpawnLocations;
    private final Material blockMaterial;
    private final int requiredBlocks;
    private final int currencyMin;
    private final int currencyMax;
    private final int durationSeconds;

    private RobberySettings(
            CuboidRegion robberyRegion,
            Location startLocation,
            Location winLocation,
            Location loseLocation,
            List<Location> blockSpawnLocations,
            Material blockMaterial,
            int requiredBlocks,
            int currencyMin,
            int currencyMax,
            int durationSeconds
    ) {
        this.robberyRegion = robberyRegion;
        this.startLocation = startLocation;
        this.winLocation = winLocation;
        this.loseLocation = loseLocation;
        this.blockSpawnLocations = List.copyOf(blockSpawnLocations);
        this.blockMaterial = blockMaterial;
        this.requiredBlocks = requiredBlocks;
        this.currencyMin = currencyMin;
        this.currencyMax = currencyMax;
        this.durationSeconds = durationSeconds;
    }

    public static RobberySettings load(JavaPlugin plugin) {
        Location min = readLocation(plugin, "RobberyLocation", "minX", "minY", "minZ");
        Location max = readLocation(plugin, "RobberyLocation", "maxX", "maxY", "maxZ");
        CuboidRegion robberyRegion = new CuboidRegion(min, max);

        List<Location> blockSpawnLocations = readSpawnLocations(plugin, robberyRegion.world());
        Material blockMaterial = Material.matchMaterial(plugin.getConfig().getString("Blocks.Material", "DIAMOND_BLOCK"));
        if (blockMaterial == null) {
            plugin.getLogger().warning("Unknown Blocks.Material value. DIAMOND_BLOCK will be used.");
            blockMaterial = Material.DIAMOND_BLOCK;
        }

        int requiredBlocks = plugin.getConfig().getInt("Blocks.Amount", blockSpawnLocations.size());
        int currencyMin = plugin.getConfig().getInt("CurrencyMin", 0);
        int currencyMax = plugin.getConfig().getInt("CurrencyMax", currencyMin);
        if (currencyMax < currencyMin) {
            plugin.getLogger().warning("CurrencyMax is lower than CurrencyMin. CurrencyMin will be used as maximum.");
            currencyMax = currencyMin;
        }

        return new RobberySettings(
                robberyRegion,
                readLocation(plugin, "RobberyTPLocation.Start", "X", "Y", "Z"),
                readLocation(plugin, "RobberyTPLocation.Win", "X", "Y", "Z"),
                readLocation(plugin, "RobberyTPLocation.Lose", "X", "Y", "Z"),
                blockSpawnLocations,
                blockMaterial,
                requiredBlocks,
                currencyMin,
                currencyMax,
                Math.max(1, plugin.getConfig().getInt("DurationSeconds", 25))
        );
    }

    private static Location readLocation(JavaPlugin plugin, String path, String xKey, String yKey, String zKey) {
        String worldName = plugin.getConfig().getString(path + ".world", "world");
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            throw new IllegalStateException("World '" + worldName + "' for config path '" + path + "' is not loaded.");
        }

        return new Location(
                world,
                plugin.getConfig().getInt(path + "." + xKey),
                plugin.getConfig().getInt(path + "." + yKey),
                plugin.getConfig().getInt(path + "." + zKey)
        );
    }

    private static List<Location> readSpawnLocations(JavaPlugin plugin, World world) {
        ConfigurationSection spawnLocations = plugin.getConfig().getConfigurationSection("Blocks.LocationSpawn");
        List<Location> locations = new ArrayList<>();
        if (spawnLocations == null) {
            plugin.getLogger().warning("Invalid Blocks.LocationSpawn format.");
            return locations;
        }

        for (String key : spawnLocations.getKeys(false)) {
            ConfigurationSection locationSection = spawnLocations.getConfigurationSection(key);
            if (locationSection == null) {
                continue;
            }
            locations.add(new Location(
                    world,
                    locationSection.getInt("X"),
                    locationSection.getInt("Y"),
                    locationSection.getInt("Z")
            ));
        }
        return locations;
    }

    public CuboidRegion robberyRegion() {
        return robberyRegion;
    }

    public Location startLocation() {
        return startLocation.clone();
    }

    public Location winLocation() {
        return winLocation.clone();
    }

    public Location loseLocation() {
        return loseLocation.clone();
    }

    public List<Location> blockSpawnLocations() {
        return blockSpawnLocations.stream().map(Location::clone).toList();
    }

    public Material blockMaterial() {
        return blockMaterial;
    }

    public int requiredBlocks() {
        return requiredBlocks;
    }

    public int currencyMin() {
        return currencyMin;
    }

    public int currencyMax() {
        return currencyMax;
    }

    public int durationSeconds() {
        return durationSeconds;
    }
}

package me.penguinx13.robberygame.service;

import me.penguinx13.robberygame.config.RobberySettings;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class BlockSpawner {

    private final RobberySettings settings;

    public BlockSpawner(RobberySettings settings) {
        this.settings = settings;
    }

    public void spawnRobberyBlocks() {
        for (Location location : settings.blockSpawnLocations()) {
            Block block = location.getBlock();
            block.setType(settings.blockMaterial());
        }
    }
}

package me.penguinx13.robberygame.model;

import org.bukkit.Location;
import org.bukkit.World;

public final class CuboidRegion {

    private final Location min;
    private final Location max;

    public CuboidRegion(Location firstCorner, Location secondCorner) {
        World world = firstCorner.getWorld();
        this.min = new Location(
                world,
                Math.min(firstCorner.getX(), secondCorner.getX()),
                Math.min(firstCorner.getY(), secondCorner.getY()),
                Math.min(firstCorner.getZ(), secondCorner.getZ())
        );
        this.max = new Location(
                world,
                Math.max(firstCorner.getX(), secondCorner.getX()),
                Math.max(firstCorner.getY(), secondCorner.getY()),
                Math.max(firstCorner.getZ(), secondCorner.getZ())
        );
    }

    public boolean contains(Location location) {
        if (location == null || location.getWorld() == null || min.getWorld() == null) {
            return false;
        }
        if (!location.getWorld().equals(min.getWorld())) {
            return false;
        }

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= min.getX() && x <= max.getX()
                && y >= min.getY() && y <= max.getY()
                && z >= min.getZ() && z <= max.getZ();
    }

    public World world() {
        return min.getWorld();
    }
}

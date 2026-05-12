package me.penguinx13.robberygame.listener;

import me.penguinx13.robberygame.service.RobberyService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class BlockBreakListener implements Listener {

    private final RobberyService robberyService;

    public BlockBreakListener(RobberyService robberyService) {
        this.robberyService = robberyService;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!robberyService.isRobberyBlock(event.getBlock().getLocation())) {
            return;
        }

        robberyService.handleRobberyBlockBreak(event.getPlayer());
    }
}

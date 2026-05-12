package me.penguinx13.robberygame.service;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class RobberySession {

    private final JavaPlugin plugin;
    private final Player player;
    private final int durationSeconds;
    private final Runnable timeoutAction;
    private final BossBar bossBar;

    private BukkitRunnable timer;
    private int secondsLeft;
    private int brokenBlocks;

    public RobberySession(JavaPlugin plugin, Player player, int durationSeconds, Runnable timeoutAction) {
        this.plugin = plugin;
        this.player = player;
        this.durationSeconds = durationSeconds;
        this.secondsLeft = durationSeconds;
        this.timeoutAction = timeoutAction;
        this.bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
        this.bossBar.addPlayer(player);
        updateBossBar();
    }

    public void startTimer() {
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (secondsLeft > 0) {
                    secondsLeft--;
                    updateBossBar();
                    return;
                }

                timeoutAction.run();
            }
        };
        timer.runTaskTimer(plugin, 20L, 20L);
    }

    public int incrementBrokenBlocks() {
        brokenBlocks++;
        updateBossBar();
        return brokenBlocks;
    }

    public int brokenBlocks() {
        return brokenBlocks;
    }

    public void finish() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        bossBar.removePlayer(player);
        bossBar.setVisible(false);
    }

    private void updateBossBar() {
        bossBar.setTitle("§fВремя: §6" + secondsLeft + " сек. §0|§f Сломано блоков: §6" + brokenBlocks);
        bossBar.setProgress(Math.max(0.0, Math.min(1.0, (double) secondsLeft / durationSeconds)));
    }
}

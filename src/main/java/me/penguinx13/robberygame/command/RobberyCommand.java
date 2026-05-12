package me.penguinx13.robberygame.command;

import me.penguinx13.robberygame.service.RobberyService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class RobberyCommand implements CommandExecutor {

    private final RobberyService robberyService;

    public RobberyCommand(RobberyService robberyService) {
        this.robberyService = robberyService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("start")) {
            sender.sendMessage("§eИспользование: /" + label + " start");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эту команду могут использовать только игроки.");
            return true;
        }

        if (!robberyService.start(player)) {
            sender.sendMessage("Подождите, сейчас идет ограбление!");
            return true;
        }

        sender.sendMessage("Ограбление началось!");
        return true;
    }
}

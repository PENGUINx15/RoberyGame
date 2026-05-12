package me.penguinx13.robberygame.command;

import me.penguinx13.robberygame.service.RobberyService;
import me.penguinx13.wapi.commands.annotations.RootCommand;
import me.penguinx13.wapi.commands.annotations.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RootCommand("robberygame")
public final class RobberyCommand {

    private final RobberyService robberyService;

    public RobberyCommand(RobberyService robberyService) {
        this.robberyService = robberyService;
    }

    @SubCommand(value = "start", playerOnly = true, description = "Начать ограбление")
    public void start(Player player) {
        if (!robberyService.start(player)) {
            player.sendMessage("Подождите, сейчас идет ограбление!");
            return;
        }

        player.sendMessage("Ограбление началось!");
    }

    @SubCommand(value = "help", description = "Показать помощь по командам ограбления")
    public void help(CommandSender sender) {
        sender.sendMessage("§6Доступные команды /robberygame:");
        sender.sendMessage(" §e/robberygame start §7- начать ограбление");
        sender.sendMessage(" §e/robberygame help §7- показать это меню");
    }
}

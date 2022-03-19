package tk.booky.hardcore.commands;
// Created by booky10 in HardCore (16:26 08.03.22)

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tk.booky.hardcore.HardCoreMain;

public class SetHeartsCommand extends CommandAPICommand implements CommandExecutor {

    private final HardCoreMain main;

    public SetHeartsCommand(HardCoreMain main) {
        super("setHearts");
        this.main = main;

        withPermission("hardcore.sethearts").executes(this);
        withArguments(new OfflinePlayerArgument("target"), new IntegerArgument("hearts", 0, 1024));
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        OfflinePlayer player = (OfflinePlayer) args[0];
        if (player != null && player.hasPlayedBefore()) {
            if ((int) args[1] > 0) {
                boolean wasBanned = false;
                if (player.isBanned()) {
                    Bukkit.getBanList(BanList.Type.NAME).pardon(player.getUniqueId().toString());
                    wasBanned = true;
                }

                main.lives().put(player.getUniqueId(), (int) args[1]);
                Player online = player.getPlayer();

                if (online != null) {
                    AttributeInstance instance = online.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                    if (instance != null) instance.setBaseValue(((int) args[1]) * 2);

                    online.setHealth(((int) args[1]) * 2);
                    online.sendHealthUpdate();
                }

                sender.sendMessage("Set hearts of " + player.getName() + " to " + args[1] + (wasBanned ? " and unbanned them" : "") + ".");
            } else if (!player.isBanned()) {
                main.lives().put(player.getUniqueId(), 10);
                player.banPlayer("You died too many times.", main.getDescription().getName());

                sender.sendMessage("Banned player " + player.getName() + ".");
            } else {
                CommandAPI.fail("Player \"" + player.getName() + "\" is already banned.");
            }
        } else {
            CommandAPI.fail("This player doesn't exists.");
        }
    }
}

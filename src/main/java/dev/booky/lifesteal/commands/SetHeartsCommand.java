package dev.booky.lifesteal.commands;
// Created by booky10 in HardCore (16:26 08.03.22)

import dev.booky.lifesteal.LifestealMain;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.Locale;

public class SetHeartsCommand extends CommandAPICommand implements CommandExecutor {

    private final LifestealMain main;

    public SetHeartsCommand(LifestealMain main) {
        super("sethearts");
        this.main = main;

        withPermission("lifesteal.command.sethearts").executes(this);
        withArguments(
            new OfflinePlayerArgument("target"),
            new IntegerArgument("hearts", 0, 1024));
    }

    @Override
    public void run(CommandSender sender, Object[] args) {
        OfflinePlayer player = (OfflinePlayer) args[0];
        if (player == null || !player.hasPlayedBefore()) {
            sender.sendMessage(Component.translatable("lifesteal.command.set-hearts.unknown-player", NamedTextColor.RED));
            return;
        }

        if ((int) args[1] == 0) {
            if (player.isBanned()) {
                sender.sendMessage(Component.translatable("lifesteal.command.set-hearts.already-banned", NamedTextColor.RED));
                return;
            }

            Locale locale = Locale.US;
            Component name = Component.text(String.valueOf(player.getName()));
            if (player.isOnline()) {
                Player target = player.getPlayer();
                if (target != null) {
                    name = target.teamDisplayName();
                    locale = target.locale();
                }
            }

            MessageFormat format = GlobalTranslator.translator().translate("lifesteal.command.set-hearts.ban-reason", locale);
            assert format != null : "format shouldn't be null";
            player.banPlayer(format.toPattern(), main.getDescription().getName());

            sender.sendMessage(Component.translatable("lifesteal.command.set-hearts.banned").args(name));
            main.lives().put(player.getUniqueId(), 10);
            return;
        }

        if (player.isBanned()) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(player.getUniqueId().toString());
        }

        Component name = Component.text(String.valueOf(player.getName()));
        Player online = player.getPlayer();

        if (online != null) {
            AttributeInstance instance = online.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (instance != null) instance.setBaseValue(((int) args[1]) * 2);

            name = online.teamDisplayName();
            online.setHealth(((int) args[1]) * 2);
            online.sendHealthUpdate();
        }

        main.lives().put(player.getUniqueId(), (int) args[1]);
        sender.sendMessage(Component.translatable("lifesteal.command.set-hearts.success").args(name, Component.text((int) args[1])));
    }
}

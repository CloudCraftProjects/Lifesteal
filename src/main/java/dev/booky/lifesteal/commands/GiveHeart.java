package dev.booky.lifesteal.commands;
// Created by booky10 in HardCore (18:31 08.03.22)

import dev.booky.lifesteal.LifestealMain;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

public class GiveHeart extends CommandAPICommand implements PlayerCommandExecutor {

    private final LifestealMain main;

    public GiveHeart(LifestealMain main) {
        super("giveheart");
        this.main = main;

        withPermission("lifesteal.command.giveheart").executesPlayer(this);
    }

    @Override
    public void run(Player sender, Object[] args) {
        int currentLives = main.lives().getOrDefault(sender.getUniqueId(), 10);
        sender.beginConversation(new Conversation(main, sender, new Prompt() {
            @Override
            public @NotNull String getPromptText(@NotNull ConversationContext context) {
                MessageFormat format = GlobalTranslator.translator().translate("lifesteal.command.give-heart.prompt", sender.locale());
                assert format != null : "format shouldn't be null";
                String text = format.toPattern();

                if (currentLives <= 1) {
                    format = GlobalTranslator.translator().translate("lifesteal.command.give-heart.warning", sender.locale());
                    assert format != null : "format shouldn't be null";
                    text += format.toPattern();
                }

                return text;
            }

            @Override
            public boolean blocksForInput(@NotNull ConversationContext context) {
                return true;
            }

            @Override
            public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                if (input == null) throw new IllegalStateException("Input was null");

                switch (input.isEmpty() ? ' ' : Character.toLowerCase(input.charAt(0))) {
                    case 'y', 'j' -> {
                        if (!sender.getInventory().addItem(main.heartItem()).isEmpty()) {
                            sender.sendMessage(Component.translatable("lifesteal.command.give-heart.inventory-full", NamedTextColor.RED));
                            return this;
                        }

                        if (currentLives > 1) {
                            main.lives().put(sender.getUniqueId(), currentLives - 1);
                            sender.sendMessage(Component.translatable("lifesteal.command.give-heart.success"));

                            AttributeInstance instance = sender.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                            if (instance != null) {
                                instance.setBaseValue((currentLives - 1) * 2);

                                if (sender.getHealth() > instance.getBaseValue()) {
                                    sender.setHealth(instance.getBaseValue());
                                }
                            }

                            sender.sendHealthUpdate();
                            return Prompt.END_OF_CONVERSATION;
                        }

                        main.lives().put(sender.getUniqueId(), 10);
                        Bukkit.broadcast(Component.translatable("lifesteal.command.give-heart.too-many.broadcast").args(sender.teamDisplayName()));

                        MessageFormat format = GlobalTranslator.translator().translate("lifesteal.command.give-heart.too-many.ban-reason", sender.locale());
                        assert format != null : "format shouldn't be null";
                        sender.banPlayer(format.toPattern(), main.getDescription().getName());
                        return Prompt.END_OF_CONVERSATION;
                    }
                    default -> {
                        sender.sendMessage(Component.translatable("lifesteal.command.give-heart.cancelled", NamedTextColor.RED));
                        return Prompt.END_OF_CONVERSATION;
                    }
                }
            }
        }) {
            {
                localEchoEnabled = false;
                modal = false;
            }
        });
    }
}

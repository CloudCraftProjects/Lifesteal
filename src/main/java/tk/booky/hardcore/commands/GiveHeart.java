package tk.booky.hardcore.commands;
// Created by booky10 in HardCore (18:31 08.03.22)

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tk.booky.hardcore.HardCoreMain;

public class GiveHeart extends CommandAPICommand implements PlayerCommandExecutor {

    private final HardCoreMain main;

    public GiveHeart(HardCoreMain main) {
        super("giveHeart");
        this.main = main;

        withPermission("hardcore.giveheart").executesPlayer(this);
    }

    @Override
    public void run(Player sender, Object[] args) {
        int currentLives = main.lives().getOrDefault(sender.getUniqueId(), 10);
        sender.beginConversation(new Conversation(main, sender, new Prompt() {
            @Override
            public @NotNull String getPromptText(@NotNull ConversationContext context) {
                return "Are you sure you want to take a heart out of you? (y/n)" +
                    (currentLives > 1 ? "" : "\nWARNING: THIS IS YOUR LAST HEART, IF YOU CONTINUE YOU WILL BE DEAD.");
            }

            @Override
            public boolean blocksForInput(@NotNull ConversationContext context) {
                return true;
            }

            @Override
            public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                if (input == null) {
                    throw new IllegalStateException("Input was null");
                } else {
                    switch (Character.toLowerCase(input.charAt(0))) {
                        case 'y', 'j' -> {
                            if (sender.getInventory().addItem(main.heartItem()).isEmpty()) {
                                if (currentLives > 1) {
                                    main.lives().put(sender.getUniqueId(), currentLives - 1);
                                    sender.sendMessage("You took a heart out of you.");

                                    AttributeInstance instance = sender.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                                    if (instance != null) {
                                        instance.setBaseValue((currentLives - 1) * 2);

                                        if (sender.getHealth() > instance.getBaseValue()) {
                                            sender.setHealth(instance.getBaseValue());
                                        }
                                    }

                                    sender.sendHealthUpdate();
                                } else {
                                    main.lives().put(sender.getUniqueId(), 10);
                                    Bukkit.broadcast(Component.text(sender.getName() + " gave away too many hearts"));
                                    sender.banPlayer("You gave away too many hearts.", main.getDescription().getName());
                                }

                                return Prompt.END_OF_CONVERSATION;
                            } else {
                                sender.sendMessage(Component.text("Your inventory is full. Try again.", NamedTextColor.RED));
                                return this;
                            }
                        }
                        default -> {
                            sender.sendMessage(Component.text("Ok, cancelled.", NamedTextColor.RED));
                            return Prompt.END_OF_CONVERSATION;
                        }
                    }
                }
            }
        }) {{
            localEchoEnabled = false;
            modal = false;
        }});
    }
}

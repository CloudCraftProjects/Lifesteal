package dev.booky.lifesteal.listener;
// Created by booky10 in HardCore (17:26 08.03.22)

import dev.booky.lifesteal.LifestealMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ReviveListener implements Listener {

    private static final Title.Times TIMES = Title.Times.times(Duration.ZERO, Duration.ofDays(42), Duration.ZERO);

    private final Map<UUID, Conversation> conversations = new HashMap<>();
    private final Map<UUID, Block> blocks = new HashMap<>();
    private final LifestealMain main;

    public ReviveListener(LifestealMain main) {
        this.main = main;
    }

    @EventHandler
    public void onExplosion(BlockExplodeEvent event) {
        event.blockList().removeIf(blocks::containsValue);
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(blocks::containsValue);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().clearTitle();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Block block = blocks.remove(event.getPlayer().getUniqueId());
        if (block != null) {
            block.setType(Material.AIR);
            if (!event.getPlayer().getInventory().addItem(main.reviveItem()).isEmpty()) {
                main.getLogger().warning("Failed to give player a revive item, dropping it.");
                block.getWorld().dropItemNaturally(event.getPlayer().getLocation(), main.reviveItem());
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getItemInHand().getType() != Material.END_ROD) return;
        if (!event.getItemInHand().getItemMeta().hasCustomModelData()) return;
        if (event.getItemInHand().getItemMeta().getCustomModelData() != 1337) return;

        if (blocks.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        blocks.put(event.getPlayer().getUniqueId(), event.getBlock());
        event.getPlayer().showTitle(Title.title(Component.empty(), Component.translatable("lifesteal.listeners.revive.title"), TIMES));

        Conversation conversation = new Conversation(main, event.getPlayer(), new Prompt() {
            @Override
            public @NotNull String getPromptText(@NotNull ConversationContext context) {
                MessageFormat format = GlobalTranslator.translator().translate("lifesteal.listeners.revive.prompt", event.getPlayer().locale());
                assert format != null : "format shouldn't be null";
                String promptText = format.toPattern();

                format = GlobalTranslator.translator().translate("lifesteal.listeners.revive.cancel-hint", event.getPlayer().locale());
                assert format != null : "format shouldn't be null";
                return promptText + format.toPattern();
            }

            @Override
            public boolean blocksForInput(@NotNull ConversationContext context) {
                return true;
            }

            @Override
            public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                if (!(context.getForWhom() instanceof Player player)) throw new IllegalStateException("The conversation context is not a player, this should be impossible");

                OfflinePlayer target = input == null ? null : Bukkit.getOfflinePlayer(input);
                if (target == null || !(target.hasPlayedBefore() || target.isOnline())) {
                    player.sendMessage(Component.translatable("lifesteal.listeners.revive.unknown-player", NamedTextColor.RED));
                    return this;
                }

                if (!target.isBanned()) {
                    player.sendMessage(Component.translatable("lifesteal.listeners.revive.not-dead", NamedTextColor.RED));
                    return this;
                }

                event.getBlock().setType(Material.AIR, true);
                event.getBlock().getWorld().strikeLightning(event.getBlock().getLocation()).setCausingPlayer(player);

                Bukkit.getBanList(BanList.Type.NAME).pardon(target.getUniqueId().toString());
                Bukkit.broadcast(Component.translatable("lifesteal.listeners.revive.sucess").args(Component.text(String.valueOf(target.getName())), player.teamDisplayName()));

                conversations.remove(player.getUniqueId());
                player.clearTitle();
                return Prompt.END_OF_CONVERSATION;
            }
        }) {{
            localEchoEnabled = false;
            modal = false;
        }};

        event.getPlayer().beginConversation(conversation);
        conversations.put(event.getPlayer().getUniqueId(), conversation);
    }

    @EventHandler
    public void onPiston(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (!blocks.containsValue(block)) continue;
            event.setCancelled(true);
            break;
        }
    }

    @EventHandler
    public void onPiston(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (!blocks.containsValue(block)) continue;
            event.setCancelled(true);
            break;
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!blocks.containsValue(event.getBlock())) return;
        Block block = blocks.get(event.getPlayer().getUniqueId());

        if (block == null || !block.getLocation().equals(event.getBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            double shiftX = random.nextDouble() * 0.5d - 0.25d;
            double shiftY = random.nextDouble() * 0.5d - 0.25d - (0.25f / 2f);
            double shiftZ = random.nextDouble() * 0.5d - 0.25d;

            Location dropLocation = event.getBlock().getLocation()
                .add(0.5d + shiftX, 0.5d + shiftY, 0.5d + shiftZ);
            dropLocation.getWorld().dropItem(dropLocation, main.reviveItem());
        }

        blocks.remove(event.getPlayer().getUniqueId());
        event.setDropItems(false);

        event.getPlayer().clearTitle();
        event.getPlayer().abandonConversation(conversations.remove(event.getPlayer().getUniqueId()));
        event.getPlayer().sendMessage(Component.translatable("lifesteal.listeners.revive.cancelled", NamedTextColor.RED));
    }
}

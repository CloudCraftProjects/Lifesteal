package tk.booky.hardcore.listener;
// Created by booky10 in HardCore (17:26 08.03.22)

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tk.booky.hardcore.HardCoreMain;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReviveListener implements Listener {

    private final Map<UUID, Conversation> conversations = new HashMap<>();
    private final Map<UUID, Block> blocks = new HashMap<>();
    private final HardCoreMain main;

    public ReviveListener(HardCoreMain main) {
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
        if (event.getItemInHand().getType() == Material.LIGHTNING_ROD
            && event.getItemInHand().getItemMeta().hasCustomModelData()
            && event.getItemInHand().getItemMeta().getCustomModelData() == 1337) {
            if (!blocks.containsKey(event.getPlayer().getUniqueId())) {
                blocks.put(event.getPlayer().getUniqueId(), event.getBlock());

                Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofDays(365), Duration.ZERO);
                Title title = Title.title(Component.empty(), Component.text("Please enter the player to revive in the chat."), times);
                event.getPlayer().showTitle(title);

                Conversation conversation = new Conversation(main, event.getPlayer(), new Prompt() {
                    @Override
                    public @NotNull String getPromptText(@NotNull ConversationContext context) {
                        return "Please enter the player to revive in the chat.";
                    }

                    @Override
                    public boolean blocksForInput(@NotNull ConversationContext context) {
                        return true;
                    }

                    @Override
                    @SuppressWarnings("deprecation")
                    public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
                        if (context.getForWhom() instanceof Player player) {
                            OfflinePlayer target = input == null ? null : Bukkit.getOfflinePlayer(input);
                            if (target != null && (target.hasPlayedBefore() || target.isOnline())) {
                                if (target.isBanned()) {
                                    Bukkit.getBanList(BanList.Type.NAME).pardon(target.getUniqueId().toString());

                                    event.getBlock().getWorld().strikeLightning(event.getBlock().getLocation()).setCausingPlayer(player);
                                    event.getBlock().setType(Material.AIR);

                                    player.sendMessage(Component.text("The player has been revived."));
                                    conversations.remove(player.getUniqueId());
                                    player.clearTitle();
                                    return Prompt.END_OF_CONVERSATION;
                                } else {
                                    player.sendMessage(Component.text("The player is not dead. Try again.", NamedTextColor.RED));
                                    return this;
                                }
                            } else {
                                player.sendMessage(Component.text("The player couldn't be found. Try again.", NamedTextColor.RED));
                                return this;
                            }
                        } else {
                            throw new IllegalStateException("The conversation context is not a player, this should be impossible");
                        }
                    }
                }) {{
                    localEchoEnabled = false;
                    modal = false;
                }};

                event.getPlayer().beginConversation(conversation);
                conversations.put(event.getPlayer().getUniqueId(), conversation);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPiston(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (blocks.containsValue(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onPiston(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (blocks.containsValue(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (blocks.containsValue(event.getBlock())) {
            Block block = blocks.get(event.getPlayer().getUniqueId());
            if (block == null || !block.getLocation().equals(event.getBlock().getLocation())) {
                event.setCancelled(true);
            } else {
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().toCenterLocation(), main.reviveItem());
                }

                blocks.remove(event.getPlayer().getUniqueId());
                event.setDropItems(false);

                event.getPlayer().sendMessage(Component.text("Cancelled.", NamedTextColor.RED));
                event.getPlayer().abandonConversation(conversations.remove(event.getPlayer().getUniqueId()));
                event.getPlayer().clearTitle();
            }
        }
    }
}

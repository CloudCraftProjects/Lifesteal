package dev.booky.lifesteal.listener;
// Created by booky10 in HardCore (16:55 17.03.22)

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AfkListener implements Listener {

    private static Field lastActionTimeField;
    private static Method getHandleMethod;

    private final Set<UUID> afkPlayers = new HashSet<>();
    private final Plugin plugin;

    public AfkListener(Plugin plugin) {
        this.plugin = plugin;

        new BukkitRunnable() {

            @Override
            public void run() {
                if (Bukkit.getIdleTimeout() <= 0) return;
                long time = System.nanoTime() / 1_000_000L;
                long idleMillis = (Bukkit.getIdleTimeout() * 60L - 1L) * 1000L;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (getHandleMethod == null && !initReflection(plugin, player.getClass())) {
                        cancel();
                        return;
                    }

                    if (afkPlayers.contains(player.getUniqueId())) {
                        if (lastActionTime(player) != Long.MAX_VALUE) {
                            plugin.getLogger().info(player.getName() + " is no longer afk");
                            afkPlayers.remove(player.getUniqueId());
                        }
                        continue;
                    }

                    if (time - lastActionTime(player) <= idleMillis) continue;
                    plugin.getLogger().info(player.getName() + " is now afk");
                    lastActionTime(player, Long.MAX_VALUE);
                    afkPlayers.add(player.getUniqueId());
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (afkPlayers.isEmpty()) return;
        if (!afkPlayers.remove(event.getPlayer().getUniqueId())) return;
        plugin.getLogger().info(event.getPlayer().getName() + " is no longer afk");
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!afkPlayers.contains(event.getEntity().getUniqueId())) return;
        event.setCancelled(true);
    }

    // TODO: update when paper merges pr #6299 // Still wasn't merged, two months later...
    private static long lastActionTime(Player player) {
        try {
            if (getHandleMethod == null) throw new IllegalStateException("Reflection not initialized");
            return lastActionTimeField.getLong(getHandleMethod.invoke(player));
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void lastActionTime(Player player, long time) {
        try {
            if (getHandleMethod == null) throw new IllegalStateException("Reflection not initialized");
            lastActionTimeField.setLong(getHandleMethod.invoke(player), time);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static boolean initReflection(Plugin plugin, Class<?> craftPlayerClass) {
        try {
            getHandleMethod = craftPlayerClass.getMethod("getHandle");
            Class<?> serverPlayerClass = getHandleMethod.getReturnType();

            try {
                lastActionTimeField = serverPlayerClass.getDeclaredField("cG");
            } catch (NoSuchFieldException ignored) {
            }

            if (lastActionTimeField == null) {
                try {
                    lastActionTimeField = serverPlayerClass.getDeclaredField("lastActionTime");
                } catch (NoSuchFieldException ignored) {
                }
            }

            if (lastActionTimeField == null) {
                // Run a tick later to show the messages at the bottom of the console.
                // Otherwise, they properly won't get noticed.
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().warning("Your version of Minecraft is not supported by this plugin.");
                    plugin.getLogger().warning("The afk player protection will not work, can't find lastActionTime field.");
                });
                return false;
            }

            lastActionTimeField.setAccessible(true);
            return true;
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}

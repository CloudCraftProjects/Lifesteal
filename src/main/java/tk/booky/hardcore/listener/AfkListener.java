package tk.booky.hardcore.listener;
// Created by booky10 in HardCore (16:55 17.03.22)

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AfkListener implements Listener {

    private static Field lastActionTimeField;
    private static Method getHandleMethod;
    private final Set<UUID> afkPlayers = new HashSet<>();

    public AfkListener(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Bukkit.getIdleTimeout() > 0) {
                long time = System.nanoTime() / 1_000_000L;
                long idleMillis = (Bukkit.getIdleTimeout() * 60L - 1L) * 1000L;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!afkPlayers.contains(player.getUniqueId())) {
                        if (time - lastActionTime(player) > idleMillis) {
                            lastActionTime(player, Long.MAX_VALUE);
                            afkPlayers.add(player.getUniqueId());
                        }
                    } else if (lastActionTime(player) != Long.MAX_VALUE) {
                        afkPlayers.remove(player.getUniqueId());
                    }
                }
            }
        }, 0, 10);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.hasExplicitlyChangedBlock()) {
            afkPlayers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && afkPlayers.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    // TODO: update when paper merges pr #6299
    private static long lastActionTime(Player player) {
        try {
            if (getHandleMethod == null) initReflection(player.getClass());
            return lastActionTimeField.getLong(getHandleMethod.invoke(player));
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void lastActionTime(Player player, long time) {
        try {
            if (getHandleMethod == null) initReflection(player.getClass());
            lastActionTimeField.setLong(getHandleMethod.invoke(player), time);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void initReflection(Class<?> craftPlayerClass) throws ReflectiveOperationException {
        getHandleMethod = craftPlayerClass.getMethod("getHandle");
        Class<?> serverPlayerClass = getHandleMethod.getReturnType();

        try {
            lastActionTimeField = serverPlayerClass.getDeclaredField("cG");
        } catch (NoSuchFieldException exception) {
            lastActionTimeField = serverPlayerClass.getDeclaredField("lastActionTime");
        }

        lastActionTimeField.setAccessible(true);
    }
}

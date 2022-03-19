package tk.booky.hardcore.listener;
// Created by booky10 in HardCore (14:24 08.03.22)

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import tk.booky.hardcore.HardCoreMain;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JoinListener implements Listener {

    private final Set<UUID> standingPlayers = new HashSet<>();
    private final HardCoreMain main;

    public JoinListener(HardCoreMain main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().discoverRecipe(main.reviveKey());
        event.getPlayer().discoverRecipe(main.heartKey());

        standingPlayers.add(event.getPlayer().getUniqueId());

        AttributeInstance instance = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (instance != null) {
            int realMaxHealth = main.lives().getOrDefault(event.getPlayer().getUniqueId(), 10) * 2;
            if (realMaxHealth != instance.getBaseValue()) {
                instance.setBaseValue(realMaxHealth);
                event.getPlayer().setHealth(realMaxHealth);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (standingPlayers.remove(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendHealthUpdate();
        }
    }
}

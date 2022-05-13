package dev.booky.lifesteal.listener;
// Created by booky10 in HardCore (14:24 08.03.22)

import dev.booky.lifesteal.LifestealMain;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JoinListener implements Listener {

    private final Set<UUID> joiningPlayers = new HashSet<>();
    private final LifestealMain main;

    public JoinListener(LifestealMain main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().discoverRecipe(main.reviveKey());
        event.getPlayer().discoverRecipe(main.heartKey());
        joiningPlayers.add(event.getPlayer().getUniqueId());

        AttributeInstance instance = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (instance == null) return;

        int realMaxHealth = main.lives().getOrDefault(event.getPlayer().getUniqueId(), 10) * 2;
        if (realMaxHealth == instance.getBaseValue()) return;

        double healthDiff = realMaxHealth - instance.getBaseValue();
        instance.setBaseValue(realMaxHealth);

        if (event.getPlayer().isDead()) return;
        double health = event.getPlayer().getHealth() + healthDiff;
        event.getPlayer().setHealth(health < 1 ? health : health > realMaxHealth ? realMaxHealth : health);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!joiningPlayers.remove(event.getPlayer().getUniqueId())) return;
        event.getPlayer().sendHealthUpdate();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        joiningPlayers.remove(event.getPlayer().getUniqueId());
    }
}

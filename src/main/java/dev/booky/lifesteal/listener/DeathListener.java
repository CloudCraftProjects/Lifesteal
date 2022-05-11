package dev.booky.lifesteal.listener;
// Created by booky10 in HardCore (15:19 08.03.22)

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import dev.booky.lifesteal.LifestealMain;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.text.MessageFormat;
import java.util.Iterator;

public record DeathListener(LifestealMain main) implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        main.lives().put(event.getPlayer().getUniqueId(), main.lives().getOrDefault(event.getPlayer().getUniqueId(), 10) - 1);

        if (!event.getKeepInventory()) {
            Iterator<ItemStack> dropIterator = event.getDrops().iterator();
            while (dropIterator.hasNext()) {
                ItemStack item = dropIterator.next();
                if (item == null) continue;
                if (!item.getItemMeta().hasCustomModelData()) continue;
                if (item.getItemMeta().getCustomModelData() != 1337) continue;

                event.getItemsToKeep().add(item);
                dropIterator.remove();
            }
        }

        event.getDrops().add(main.heartItem());
    }

    @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        int targetHealthPoints = main.lives().getOrDefault(event.getPlayer().getUniqueId(), 10) * 2;
        if (targetHealthPoints <= 0) {
            main.lives().put(event.getPlayer().getUniqueId(), 10);
            Bukkit.broadcast(Component.translatable("lifesteal.listeners.death.broadcast").args(event.getPlayer().teamDisplayName()));

            MessageFormat format = GlobalTranslator.translator().translate("lifesteal.listeners.death.ban-reason", event.getPlayer().locale());
            assert format != null : "format shouldn't be null";
            event.getPlayer().banPlayer(format.toPattern(), main.getDescription().getName());
            return;
        }

        AttributeInstance attribute = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute == null) return;

        attribute.setBaseValue(targetHealthPoints);
        event.getPlayer().setHealth(targetHealthPoints);
    }
}

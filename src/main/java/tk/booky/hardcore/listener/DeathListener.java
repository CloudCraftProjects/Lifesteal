package tk.booky.hardcore.listener;
// Created by booky10 in HardCore (15:19 08.03.22)

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import tk.booky.hardcore.HardCoreMain;

import java.util.Iterator;

public record DeathListener(HardCoreMain main) implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        main.lives().put(event.getPlayer().getUniqueId(), main.lives().getOrDefault(event.getPlayer().getUniqueId(), 10) - 1);

        if (!event.getKeepInventory()) {
            Iterator<ItemStack> dropsIterator = event.getDrops().iterator();
            while (dropsIterator.hasNext()) {
                ItemStack item = dropsIterator.next();
                if (item != null && item.getType() == Material.HONEY_BOTTLE
                    && item.getItemMeta().hasCustomModelData()
                    && item.getItemMeta().getCustomModelData() == 1337) {
                    event.getItemsToKeep().add(item);
                    dropsIterator.remove();
                }
            }
        }

        event.getDrops().add(main.heartItem());
    }

    @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        int targetHealthPoints = main.lives().getOrDefault(event.getPlayer().getUniqueId(), 10) * 2;
        if (targetHealthPoints > 0) {
            AttributeInstance attribute = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute != null) {
                attribute.setBaseValue(targetHealthPoints);
                event.getPlayer().setHealth(targetHealthPoints);
            }
        } else {
            main.lives().put(event.getPlayer().getUniqueId(), 10);
            event.getPlayer().banPlayer("You died too many times.", main.getDescription().getName());
            Bukkit.broadcast(Component.text(event.getPlayer().getName() + " died too many times"));
        }
    }
}

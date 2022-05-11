package dev.booky.lifesteal.listener;
// Created by booky10 in HardCore (15:31 08.03.22)

import dev.booky.lifesteal.LifestealMain;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public record HeartListener(LifestealMain main) implements Listener {

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != Material.HONEY_BOTTLE) return;
        if (!event.getItem().getItemMeta().hasCustomModelData()) return;
        if (event.getItem().getItemMeta().getCustomModelData() != 1337) return;

        int newLives = main.lives().getOrDefault(event.getPlayer().getUniqueId(), 10) + 1;
        main.lives().put(event.getPlayer().getUniqueId(), newLives);

        AttributeInstance attribute = event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute != null) attribute.setBaseValue(newLives * 2);
        event.getPlayer().setHealth(event.getPlayer().getHealth() + 2);

        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_TOTEM_USE,
            SoundCategory.AMBIENT, Float.MAX_VALUE, 0.9f);
        event.setCancelled(true);

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() == Material.HONEY_BOTTLE
            && item.getItemMeta().hasCustomModelData()
            && item.getItemMeta().getCustomModelData() == 1337) {
            item.setAmount(item.getAmount() - 1);
        } else {
            (item = event.getPlayer().getInventory().getItemInOffHand()).setAmount(item.getAmount() - 1);
        }
    }
}

package dev.booky.lifesteal.listener;
// Created by booky10 in HardCore (21:02 09.05.22)

import dev.booky.lifesteal.LifestealMain;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public record CustomItemListener(LifestealMain main) implements Listener {

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack item = event.getInventory().getFirstItem();
        if (item == null) return;
        if (!item.getItemMeta().hasCustomModelData()) return;
        if (item.getItemMeta().getCustomModelData() != 1337) return;
        event.setResult(new ItemStack(Material.AIR));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item)) return;
        if (!item.getItemStack().getItemMeta().hasCustomModelData()) return;
        if (item.getItemStack().getItemMeta().getCustomModelData() != 1337) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        if (!item.getItemMeta().hasCustomModelData()) return;
        if (item.getItemMeta().getCustomModelData() != 1337) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() == null) return;
        if (event.getInventory().getResult() == null) return;

        for (ItemStack item : event.getInventory().getMatrix()) {
            if (item == null) continue;
            if (!item.getItemMeta().hasCustomModelData()) continue;
            if (item.getItemMeta().getCustomModelData() != 1337) continue;
            event.getInventory().setResult(null);
            break;
        }
    }
}

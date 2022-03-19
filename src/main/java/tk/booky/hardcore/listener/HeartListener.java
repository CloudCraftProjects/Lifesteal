package tk.booky.hardcore.listener;
// Created by booky10 in HardCore (15:31 08.03.22)

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import tk.booky.hardcore.HardCoreMain;

public record HeartListener(HardCoreMain main) implements Listener {

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack item = event.getInventory().getFirstItem();
        if (item != null && item.getItemMeta().hasCustomModelData()
            && item.getItemMeta().getCustomModelData() == 1337) {
            event.setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item item) {
            if (item.getItemStack().getItemMeta().hasCustomModelData()
                && item.getItemStack().getItemMeta().getCustomModelData() == 1337) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        if (item.getItemMeta().hasCustomModelData()
            && item.getItemMeta().getCustomModelData() == 1337) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.HONEY_BOTTLE
            && event.getItem().getItemMeta().hasCustomModelData()
            && event.getItem().getItemMeta().getCustomModelData() == 1337) {
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

    @EventHandler
    public void onItemCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe() != null) {
            for (ItemStack item : event.getInventory().getMatrix()) {
                if (item != null && item.getType() == Material.HONEY_BOTTLE
                    && item.getItemMeta().hasCustomModelData()
                    && item.getItemMeta().getCustomModelData() == 1337) {
                    event.getInventory().setResult(null);
                    break;
                }
            }
        }
    }
}

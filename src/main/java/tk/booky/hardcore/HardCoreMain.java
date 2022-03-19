package tk.booky.hardcore;
// Created by booky10 in HardCore (14:40 06.03.22)

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import tk.booky.hardcore.commands.GiveHeart;
import tk.booky.hardcore.commands.SetHeartsCommand;
import tk.booky.hardcore.listener.AfkListener;
import tk.booky.hardcore.listener.DeathListener;
import tk.booky.hardcore.listener.HeartListener;
import tk.booky.hardcore.listener.JoinListener;
import tk.booky.hardcore.listener.ReviveListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public class HardCoreMain extends JavaPlugin {

    private final Map<UUID, Integer> lives = new HashMap<>();
    private NamespacedKey heartKey, reviveKey;
    private ItemStack heartItem, reviveItem;

    @Override
    public void onLoad() {
        try {
            Class.forName("dev.jorel.commandapi.CommandAPI");

            new SetHeartsCommand(this).register();
            new GiveHeart(this).register();
        } catch (ClassNotFoundException exception) {
            getLogger().warning("No commands will be registered, because commandapi is not installed.");
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        readConfig();

        (heartItem = new ItemStack(Material.HONEY_BOTTLE)).editMeta(meta -> {
            meta.setCustomModelData(1337);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.LOYALTY, 1, true);
            meta.displayName(text("Extra Heart", LIGHT_PURPLE).decoration(ITALIC, false));
        });

        (reviveItem = new ItemStack(Material.LIGHTNING_ROD)).editMeta(meta -> {
            meta.setCustomModelData(1337);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.LOYALTY, 1, true);
            meta.displayName(text("Revive", LIGHT_PURPLE).decoration(ITALIC, false));
        });

        heartKey = new NamespacedKey(this, "extra_heart");
        Bukkit.addRecipe(new ShapedRecipe(heartKey, heartItem) {{
            shape("DND", "GHG", "DND");
            setIngredient('D', Material.DIAMOND);
            setIngredient('N', Material.NETHERITE_INGOT);
            setIngredient('G', Material.GOLD_BLOCK);
            setIngredient('H', Material.HEART_OF_THE_SEA);
        }});

        reviveKey = new NamespacedKey(this, "revive");
        Bukkit.addRecipe(new ShapedRecipe(reviveKey, reviveItem) {{
            shape("NDN", "XTX", "NDN");
            setIngredient('N', Material.NETHERITE_INGOT);
            setIngredient('D', Material.DIAMOND_BLOCK);
            setIngredient('X', new RecipeChoice.MaterialChoice(Material.NETHER_STAR,
                Material.ENCHANTED_GOLDEN_APPLE));
            setIngredient('T', Material.TOTEM_OF_UNDYING);
        }});

        Bukkit.getPluginManager().registerEvents(new AfkListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HeartListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ReviveListener(this), this);
    }

    @Override
    public void onDisable() {
        writeConfig();

        Bukkit.removeRecipe(heartKey);
    }

    public Map<UUID, Integer> lives() {
        return lives;
    }

    public void readConfig() {
        ConfigurationSection livesSection = getConfig().getConfigurationSection("lives");
        if (livesSection != null) {
            for (String uuidString : livesSection.getKeys(false)) {
                lives.put(UUID.fromString(uuidString), livesSection.getInt(uuidString, 10));
            }
        }
    }

    public void writeConfig() {
        getConfig().set("lives", null);
        ConfigurationSection livesSection = getConfig().createSection("lives");

        for (Map.Entry<UUID, Integer> entry : lives.entrySet()) {
            livesSection.set(entry.getKey().toString(), entry.getValue());
        }

        saveConfig();
    }

    public ItemStack reviveItem() {
        return reviveItem;
    }

    public ItemStack heartItem() {
        return heartItem;
    }

    public NamespacedKey reviveKey() {
        return reviveKey;
    }

    public NamespacedKey heartKey() {
        return heartKey;
    }
}

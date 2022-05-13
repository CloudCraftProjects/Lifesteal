package dev.booky.lifesteal;
// Created by booky10 in HardCore (14:40 06.03.22)

import dev.booky.lifesteal.commands.GiveHeart;
import dev.booky.lifesteal.commands.SetHeartsCommand;
import dev.booky.lifesteal.listener.AfkListener;
import dev.booky.lifesteal.listener.CustomItemListener;
import dev.booky.lifesteal.listener.DeathListener;
import dev.booky.lifesteal.listener.HeartListener;
import dev.booky.lifesteal.listener.JoinListener;
import dev.booky.lifesteal.listener.ReviveListener;
import dev.booky.lifesteal.util.StaticTranslator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifestealMain extends JavaPlugin {

    private final Map<UUID, Integer> lives = new HashMap<>();
    private StaticTranslator translator;
    private NamespacedKey heartKey, reviveKey;
    private ItemStack heartItem, reviveItem;
    private boolean commandApiFound, cloudPlaneFound;

    @Override
    public void onLoad() {
        try {
            Class.forName("dev.jorel.commandapi.CommandAPI");
            commandApiFound = true;

            new SetHeartsCommand(this).register();
            new GiveHeart(this).register();
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("dev.booky.cloudplane.CloudPlaneConfig");
            cloudPlaneFound = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        readConfig();

        (heartItem = new ItemStack(Material.HONEY_BOTTLE)).editMeta(meta -> {
            Component name = (cloudPlaneFound ? Component.translatable("lifesteal.item.extra-heart", NamedTextColor.LIGHT_PURPLE)
                    : Component.text("Extra Heart", NamedTextColor.LIGHT_PURPLE)).decoration(TextDecoration.ITALIC, false);

            meta.setCustomModelData(1337);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.LOYALTY, 1, true);
            meta.displayName(name);
        });

        (reviveItem = new ItemStack(Material.END_ROD)).editMeta(meta -> {
            Component name = (cloudPlaneFound ? Component.translatable("lifesteal.item.revive", NamedTextColor.LIGHT_PURPLE)
                    : Component.text("Revive", NamedTextColor.LIGHT_PURPLE)).decoration(TextDecoration.ITALIC, false);

            meta.setCustomModelData(1337);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.LOYALTY, 1, true);
            meta.displayName(name);
        });

        heartKey = new NamespacedKey(this, "extra_heart");
        Bukkit.addRecipe(new ShapedRecipe(heartKey, heartItem) {{
            shape("NDN", "XTX", "NDN");
            setIngredient('N', Material.NETHERITE_INGOT);
            setIngredient('D', Material.DIAMOND_BLOCK);
            setIngredient('X', new RecipeChoice.MaterialChoice(Material.NETHER_STAR, Material.ENCHANTED_GOLDEN_APPLE));
            setIngredient('T', Material.TOTEM_OF_UNDYING);
        }});

        reviveKey = new NamespacedKey(this, "revive");
        Bukkit.addRecipe(new ShapedRecipe(reviveKey, reviveItem) {{
            shape("DND", "GHG", "DND");
            setIngredient('D', Material.DIAMOND);
            setIngredient('N', Material.NETHERITE_INGOT);
            setIngredient('G', Material.GOLD_BLOCK);
            setIngredient('H', Material.HEART_OF_THE_SEA);
        }});

        Bukkit.getPluginManager().registerEvents(new AfkListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HeartListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ReviveListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CustomItemListener(this), this);

        GlobalTranslator.translator().addSource(translator = new StaticTranslator(this));

        if (!commandApiFound || !cloudPlaneFound) {
            // Wait one tick before executing the warning.
            // Otherwise, nobody would notice it.
            Bukkit.getScheduler().runTask(this, () -> {
                if (!commandApiFound) {
                    getLogger().warning("No commands will be registered, because CommandAPI is not installed.");
                    getLogger().warning("Download it at https://github.com/JorelAli/CommandAPI/releases.");
                }

                if (!cloudPlaneFound) {
                    getLogger().warning("Items won't be translated. Install CloudPlane and enable");
                    getLogger().warning("localized items, to fix this issue.");
                }
            });
        }
    }

    @Override
    public void onDisable() {
        writeConfig();
        Bukkit.removeRecipe(heartKey);
        Bukkit.removeRecipe(reviveKey);
        GlobalTranslator.translator().removeSource(translator);
    }

    public Map<UUID, Integer> lives() {
        return lives;
    }

    public void readConfig() {
        ConfigurationSection livesSection = getConfig().getConfigurationSection("lives");
        if (livesSection == null) return;

        for (String uuidString : livesSection.getKeys(false)) {
            lives.put(UUID.fromString(uuidString), livesSection.getInt(uuidString, 10));
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

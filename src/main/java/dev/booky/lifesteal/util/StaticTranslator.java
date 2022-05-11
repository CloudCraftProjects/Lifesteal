package dev.booky.lifesteal.util;
// Created by booky10 in HardCore (21:11 09.05.22)

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.Translator;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;

public record StaticTranslator(Plugin plugin) implements Translator {

    @SuppressWarnings("PatternValidation")
    @Override
    public @NotNull Key name() {
        return Key.key(plugin.getName().toLowerCase(), "i18n");
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        String text = switch (locale.getCountry()) {
            case "DE", "AT", "CH" -> switch (key) {
                case "lifesteal.command.give-heart.prompt" -> "Bist du dir sicher, das du ein Leben von dir nehmen möchtest? (j/n)";
                case "lifesteal.command.give-heart.warning" -> "WARNUNG: DIES IST DEIN LETZTES LEBEN, DU STIRBST, WENN DU WEITERMACHST.";
                case "lifesteal.command.give-heart.inventory-full" -> "Dein Inventar ist voll. Versuche es nochmal.";
                case "lifesteal.command.give-heart.success" -> "Du hast ein Leben aus dir genommen.";
                case "lifesteal.command.give-heart.too-many.broadcast" -> "$0 hat zu viele Leben vergeben";
                case "lifesteal.command.give-heart.too-many.ban-reason" -> "Du hast zu viele Leben vergeben";
                case "lifesteal.command.give-heart.cancelled" -> "Ok, abgebrochen.";
                case "lifesteal.command.set-hearts.unknown-player" -> "Der Spieler wurde nicht gefunden.";
                case "lifesteal.command.set-hearts.already-banned" -> "Der Spieler ist schon gebannt.";
                case "lifesteal.command.set-hearts.ban-reason" , "lifesteal.listeners.death.ban-reason" -> "Du bist zu oft gestorben";
                case "lifesteal.command.set-hearts.banned" -> "$0 wurde gebannt.";
                case "lifesteal.command.set-hearts.success" -> "Die Leben von $0 wurden auf $1 gesetzt.";
                case "lifesteal.listeners.death.broadcast" -> "$0 ist zu häufig gestorben";
                case "lifesteal.listeners.revive.title" -> "Bitte gib den wiederzubelebenden Spieler in den Chat ein";
                case "lifesteal.listeners.revive.prompt" -> "Bitte gib den wiederzubelebenden Spieler in den Chat ein.";
                case "lifesteal.listeners.revive.cancel-hint" -> "Bau den Block ab, um die Wiederbelebung abzubrechen.";
                case "lifesteal.listeners.revive.unknown-player" -> "Der Spieler konnte nicht gefunden werden. Versuche es nochmal.";
                case "lifesteal.listeners.revive.not-dead" -> "Der Spieler ist nicht Tot. Versuche es nochmal.";
                case "lifesteal.listeners.revive.success" -> "$0 wurde von $1 wiederbelebt.";
                case "lifesteal.listeners.revive.cancelled" -> "Wiederbelebung abgebrochen.";
                case "lifesteal.item.extra-heart" -> "Extra-Leben";
                case "lifesteal.item.revive" -> "Wiederbelebung";
                default -> null;
            };
            default -> switch (key) {
                case "lifesteal.command.give-heart.prompt" -> "Are you sure you want to take a heart out of you? (y/n)";
                case "lifesteal.command.give-heart.warning" -> "WARNING: THIS IS YOUR LAST LIFE, YOU WILL DIE IF YOU CONTINUE.";
                case "lifesteal.command.give-heart.inventory-full" -> "Your inventory is full. Try again.";
                case "lifesteal.command.give-heart.success" -> "You took a heart out of you.";
                case "lifesteal.command.give-heart.too-many.broadcast" -> "$0 gave away too many hearts";
                case "lifesteal.command.give-heart.too-many.ban-reason" -> "You gave away too many hearts";
                case "lifesteal.command.give-heart.cancelled" -> "Ok, cancelled.";
                case "lifesteal.command.set-hearts.unknown-player" -> "The player could not be found.";
                case "lifesteal.command.set-hearts.already-banned" -> "The player is already banned.";
                case "lifesteal.command.set-hearts.ban-reason", "lifesteal.listeners.death.ban-reason" -> "You died too many times";
                case "lifesteal.command.set-hearts.banned" -> "$0 has been banned.";
                case "lifesteal.command.set-hearts.success" -> "The hearts of $0 have been set to $1.";
                case "lifesteal.listeners.death.broadcast" -> "$0 died too many times";
                case "lifesteal.listeners.revive.title" -> "Please enter the player to revive in the chat";
                case "lifesteal.listeners.revive.prompt" -> "Please enter the player to revive in the chat.";
                case "lifesteal.listeners.revive.cancel-hint" -> "Destroy the block to cancel the revive.";
                case "lifesteal.listeners.revive.unknown-player" -> "The player couldn't be found. Try again.";
                case "lifesteal.listeners.revive.not-dead" -> "The player is not dead. Try again.";
                case "lifesteal.listeners.revive.success" -> "$0 has been revived by $1.";
                case "lifesteal.listeners.revive.cancelled" -> "Cancelled revive.";
                case "lifesteal.item.extra-heart" -> "Extra Heart";
                case "lifesteal.item.revive" -> "Revive";
                default -> null;
            };
        };

        return text == null ? null : new MessageFormat(text, locale);
    }
}

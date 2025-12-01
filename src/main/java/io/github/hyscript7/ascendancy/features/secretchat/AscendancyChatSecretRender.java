package io.github.hyscript7.ascendancy.features.secretchat;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.github.hyscript7.ascendancy.features.innate.names.InnateUtils;
import io.github.hyscript7.ascendancy.features.magic.Spell;
import io.github.hyscript7.ascendancy.features.magic.SpellUtility;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AscendancyChatSecretRender implements ChatRenderer {
    private static final double innateNameRevealDistance = 48.0d;
    private final String honeypotName = AscendancyConfig.getInstance().getInnateNames().honeypot().name();
    private final boolean honeypotEnabled = AscendancyConfig.getInstance().getInnateNames().honeypot().enabled();

    public Component obfuscateName(String originalName) {
        String display = honeypotEnabled ? honeypotName : "*".repeat(originalName.length());

        Component base = Component.text(display)
                .style(Style.style(TextDecoration.OBFUSCATED));

        // TODO: Format
        Component hover = Component.text(
                "You have not heard this innate name yet.\n" +
                        "Try listening a little closer to see if you can hear someone whisper it."
        );

        return base.hoverEvent(HoverEvent.showText(hover));
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        ChatRenderer defaultRender = ChatRenderer.defaultRenderer(); // Might be unnecessary
        if (!(viewer instanceof Player player)) return defaultRender.render(source, sourceDisplayName, message, viewer);

        String content = (message instanceof TextComponent tc)
                ? tc.content()
                : "";

        // Honeypot check
        if (honeypotEnabled &&
                content.toLowerCase().contains(honeypotName.toLowerCase())) {
            return defaultRender.render(source, sourceDisplayName, Component.text("I am a fool who thought Script doesn't know how obfuscated text works!"), viewer);
        }

        // Innate name detection
        String name = InnateUtils.findFirstValidInnateName(content, 0);
        // If a name was found
        if (name != null) {

            // Get the owner of the name
            UUID uuid = TrueNameManager.getInstance().findTrueNameOwner(name);
            Player owner = uuid != null ? Bukkit.getPlayer(uuid) : null;

            // If the owner exists
            if (owner != null) {
                PlayerData viewerData = PlayerDataManager.getInstance().getPlayerData(player);
                // If it was near me, learn the name
                if (source.getLocation().distance(player.getLocation()) < innateNameRevealDistance) {
                    viewerData.learnName(viewerData.getTrueName());
                    AscendancyMessagingAPI.getInstance().send(player, AscendancyMessagingAPI.MessageType.INFO, "You have learned " + owner.getName() + "'s true name: " + name);
                }
                // Check if I know this name
                if (viewerData.knowsTrueName(name)) {
                    // I know the name! If it's mine, underline it and color it orange, otherwise purple
                    Style style;
                    if (viewerData.getTrueName().equalsIgnoreCase(name)) {
                        style = Style.style(TextColor.color(0xB9926A), TextDecoration.UNDERLINED);
                        // Play sound to alert us of our name being used.
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                    } else {
                        style = Style.style(TextColor.color(0x6933b9));
                    }
                    // Replace the name string with the colored one
                    message = message.replaceText(builder ->
                            builder.matchLiteral(name)
                                    .replacement(Component.text(name).style(style))
                    );
                } else {
                    // I don't know this name, obfuscate it
                    message = message.replaceText(builder ->
                            builder.matchLiteral(name)
                                    .replacement(obfuscateName(name))
                    );
                }
            }
        }

        // Spell Detection
        Spell spell = SpellUtility.getSpellFromString(content);
        if (spell != null) {
            message = message.append(Component.text(" ")
                    .append(Component.text("Ⓘ")
                            .hoverEvent(
                                    HoverEvent.showText(
                                            Component.text("Spell: " + spell.getDisplayName() + "\nID: " + spell.getId())
                                    )
                            ).style(
                                    Style.style(TextColor.color(0x6933b9))
                            )
                    )
            );
        }

        return defaultRender.render(source, sourceDisplayName, message, viewer);
    }
}

package io.github.hyscript7.ascendancy.features.innate.names;

import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class InnateNameHiderRender implements ChatRenderer {
    // TODO: Load from config
    private Optional<String> honeypotName = Optional.empty();

    public Component obfuscateName(String originalName) {

        String display = honeypotName.orElse("*".repeat(originalName.length()));

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
    public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {

        String honeypot = honeypotName.orElse(null);

        String content = (message instanceof TextComponent tc)
                ? tc.content()
                : "";

        // Honeypot check
        if (honeypot != null &&
                content.toLowerCase().contains(honeypot.toLowerCase())) {

            return Component.translatable(
                    "chat.type.text",
                    sourceDisplayName,
                    Component.text("I am a fool who thought Script doesn't know how obfuscated text works!")
            );
        }

        // Innate name detection
        String name = InnateUtils.findFirstValidInnateName(content, 0);
        if (name != null) {

            UUID uuid = TrueNameManager.getInstance().findTrueNameOwner(name);
            Player owner = uuid != null ? Bukkit.getPlayer(uuid) : null;

            if (owner != null) {
                message = message.replaceText(builder ->
                        builder.matchLiteral(name)
                                .replacement(obfuscateName(name))
                );
            }
        }

        return Component.translatable("chat.type.text", sourceDisplayName, message);
    }
}

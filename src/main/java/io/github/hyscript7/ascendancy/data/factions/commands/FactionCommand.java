package io.github.hyscript7.ascendancy.data.factions.commands;

import io.github.hyscript7.ascendancy.data.factions.FactionManager;
import io.github.hyscript7.ascendancy.data.factions.simple.Faction;
import io.github.hyscript7.ascendancy.data.factions.simple.FactionMember;
import io.github.hyscript7.ascendancy.data.factions.simple.FullFactionMember;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;

public class FactionCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        // Check if the sender is a Player
        if (!(source.getSender() instanceof Player)) {
            return;
        }
        Player player = (Player) source.getSender();

        FactionManager fm = FactionManager.getInstance();
        if (args.length == 0) {
            FullFactionMember member = fm.getFactionMember(player.getUniqueId());
            if (member == null) {
                player.sendMessage("Not a faction member");
            } else {
                player.sendMessage(member.getFaction()+": "+member.getPermission());
            }
            return;
        }

        if (args.length == 1) {
            switch (args[0]) {
                case "info" -> {
                    player.sendMessage("Factions:");
                    for (Faction f : fm.getFactions()) {
                        player.sendMessage(f.toString());
                    }
                }
            }
        }

        if (args.length == 2) {
            switch (args[0]) {
                case "create" -> {
                    if (fm.getFactions().stream().anyMatch(a -> a.getName().equals(args[1]))) {
                        player.sendMessage("Faction already exists");
                        return;
                    }
                    fm.createFaction(args[1], player.getUniqueId());
                    player.sendMessage("Faction "+args[1]+" created");
                }
                case "delete" -> {
                    // needs permissions
                    fm.deleteFaction(args[1]);
                    player.sendMessage("Faction "+args[1]+" deleted");
                }
                case "join" -> {
                    if (fm.getFactionMember(player.getUniqueId()) != null) {
                        player.sendMessage("Player "+args[1]+" already joined a faction");
                        return;
                    }
                    fm.addMember(args[1], player.getUniqueId());
                    player.sendMessage("Added "+player.getName()+" to "+args[1]+" created");
                }
            }
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        if (args.length < 2) {
            return Arrays.stream(new String[]{"create", "delete", "join", "info"}).toList();
        }

        return Arrays.stream(new String[]{}).toList();
    }
}
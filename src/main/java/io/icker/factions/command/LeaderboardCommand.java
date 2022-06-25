package io.icker.factions.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.icker.factions.FactionsMod;
import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import io.icker.factions.core.FactionsManager;
import io.icker.factions.util.Command;
import io.icker.factions.util.Message;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardCommand  implements Command {
    private int self(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        User user = User.get(player.getUuid());
        Faction userFaction = user.getFaction();
        List<Faction> factions = new java.util.ArrayList<>(Faction.all()
                .stream().sorted(Comparator.comparingInt(Faction::getPower))
                .limit(9).toList());
        Collections.reverse(factions);
        Message message = new Message(Formatting.BOLD + "Factions Leaderboard:\n");
        for (int i = 0; i < factions.size(); i++){
            Faction faction = factions.get(i);
            String mod = faction.getColor().toString();
            if(userFaction != null && userFaction.getID() == faction.getID())
                mod += Formatting.BOLD.toString();
            message.add(mod + "    "+ faction.getName() + ": " + faction.getPower() + (i < factions.size() - 1 ? "\n" : ""));
        }
        message.send(player, false);

        return 1;
    }
    public LiteralCommandNode<ServerCommandSource> getNode() {
        return CommandManager
                .literal("leaderboard")
                .requires(Requires.hasPerms("factions.leaderboard", 0))
                .executes(this::self)
                .build();
    }
}

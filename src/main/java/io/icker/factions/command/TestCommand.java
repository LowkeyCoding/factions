package io.icker.factions.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.icker.factions.api.Faction;
import io.icker.factions.api.Player;
import io.icker.factions.util.Command;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TestCommand implements Command {
    private int run(CommandContext<ServerCommandSource> context, Player player, ServerPlayerEntity entity) {
        Faction faction = player.getFaction();
        System.out.println(faction);
        return 1;
    }

    public LiteralCommandNode<ServerCommandSource> getNode() {
        return CommandManager
            .literal("test")
            .requires(Requires.isMember())
            .executes(Executes.execute(this::run))
            .build();
    }
}
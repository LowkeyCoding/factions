package io.icker.factions.command;

import java.util.ArrayList;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.icker.factions.api.Faction;
import io.icker.factions.api.Player;
import io.icker.factions.util.Command;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ListCommand implements Command {
    public int run(CommandContext<ServerCommandSource> context, Player player, ServerPlayerEntity entity) {
        ArrayList<Faction> factions = Faction.all();
        int size = factions.size();

        /*
        new Message("There %s ", size == 1 ? "is" : "are")
            .add(new Message(String.valueOf(size)).format(Formatting.YELLOW))
            .add(" faction%s", size == 1 ? "" : "s")
            .send(source.getPlayer(), false);
        */

        factions.forEach(f -> InfoCommand.info(player, f));
        return 1;
    }

    public LiteralCommandNode<ServerCommandSource> getNode() {
		return CommandManager
			.literal("list")
			.executes(Executes.execute(this::run))
			.build();
    }
}
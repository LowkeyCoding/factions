package io.icker.factions.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.icker.factions.api.Player;
import io.icker.factions.util.Command;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public class ModifyCommand implements Command {
    public int description(CommandContext<ServerCommandSource> context, Player player, ServerPlayerEntity entity) {
        String description = StringArgumentType.getString(context, "description");

        player.getFaction().setDescription(description);

		//new Message("Successfully updated faction description").send(entity, false);
		return 1;
    }

    public int color(CommandContext<ServerCommandSource> context, Player player, ServerPlayerEntity entity) {
        Formatting color = ColorArgumentType.getColor(context, "color");

		player.getFaction().setColor(color.getName());
		//new Message("Successfully updated faction color").send(player, false);
		return 1;
    }

    public int open(CommandContext<ServerCommandSource> context, Player player, ServerPlayerEntity entity) {
        boolean open = BoolArgumentType.getBool(context, "open");

		player.getFaction().setOpen(open);
		//new Message("Successfully updated faction to  " + (open ? "open" : "closed")).send(player, false);
		return 1;
	}

    public LiteralCommandNode<ServerCommandSource> getNode() {
		return CommandManager
			.literal("modify")
			.requires(Requires.isCommander())
            .then(
                CommandManager.literal("description")
			    .then(
                    CommandManager.argument("description", StringArgumentType.greedyString())
                    .executes(Executes.execute(this::description))
                )
            )
            .then(
                CommandManager.literal("color")
			    .then(
                    CommandManager.argument("color", ColorArgumentType.color())
                    .executes(Executes.execute(this::color))
                )
            )
            .then(
                CommandManager.literal("open")
                .then(
                    CommandManager.argument("open", BoolArgumentType.bool())
                    .executes(Executes.execute(this::open))
                )
            )
			.build();
    }
}

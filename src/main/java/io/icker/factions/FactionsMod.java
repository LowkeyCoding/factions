package io.icker.factions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.icker.factions.command.ModifyCommand;
import io.icker.factions.command.TestCommand;
import io.icker.factions.config.Config;
import io.icker.factions.database.Database;
import io.icker.factions.util.Command;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class FactionsMod implements ModInitializer {
	public static Logger LOGGER = LogManager.getLogger("factions");

	@Override
	public void onInitialize() {
		LOGGER.info("Initalized Factions Mod");
		Config.load();
		Database.connect(Config.DB_URL);
	}

	public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> factions = CommandManager
			.literal("factions")
			.build();

		LiteralCommandNode<ServerCommandSource> alias = CommandManager
			.literal("f")
			.redirect(factions)
			.build();

		dispatcher.getRoot().addChild(factions);
		dispatcher.getRoot().addChild(alias);

		Command[] commands = new Command[] {
			new TestCommand(),
			new ModifyCommand()
		};

		for (Command command : commands) {
			factions.addChild(command.getNode());
		}
	}
}

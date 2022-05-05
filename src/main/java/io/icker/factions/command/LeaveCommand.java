package io.icker.factions.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.icker.factions.api.Faction;
import io.icker.factions.api.Member;
import io.icker.factions.api.Player;
import io.icker.factions.util.Command;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class LeaveCommand implements Command {
	public int run(CommandContext<ServerCommandSource> context, Player player, ServerPlayerEntity entity) {		
		Member member = Member.get(player.uuid);
		Faction faction = member.getFaction();
        
		// TODO: v1.4 REWRITE LOGIC
		// ALL HERE

		new Message(player.getName().asString() + " left").send(faction);
		member.remove();
        context.getSource().getServer().getPlayerManager().sendCommandTree(player);

		if (faction.getMembers().size() == 0) {
			faction.remove();
		} else {
			FactionEvents.adjustPower(faction, -Config.MEMBER_POWER);
		}
		
		return 1;
	}

	public LiteralCommandNode<ServerCommandSource> getNode() {
		// TODO Auto-generated method stub
		return null;
	}
}
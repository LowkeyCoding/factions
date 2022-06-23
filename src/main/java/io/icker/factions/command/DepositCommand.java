package io.icker.factions.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.icker.factions.FactionsMod;
import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import io.icker.factions.util.Command;
import io.icker.factions.util.Message;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class DepositCommand  implements Command {

    private int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        User user = User.get(player.getUuid());
        Faction faction = user.getFaction();
        ItemStack item =  player.getInventory().getMainHandStack();
        int id = Item.getRawId(item.getItem());
        int count = item.getCount();
        if(id == FactionsMod.CONFIG.DEPOSIT_ITEM_ID){
            new Message("%s: Deposited %d %s%s for %d power",
                    player.getName().getString(), count, item.getName().getString(),
                    count > 1 ? "'s" : "", FactionsMod.CONFIG.DEPOSIT_POWER*count)
                    .send(faction);
            item.decrement(count);
            faction.adjustPower(FactionsMod.CONFIG.DEPOSIT_POWER*count);
        } else {
            new Message(item.getName() + " Cannot be used to deposit! Only diamonds are allowed").fail().send(player, false);
            return 0;
        }
        return 1;
    }

    public LiteralCommandNode<ServerCommandSource> getNode() {
        return CommandManager
                .literal("deposit")
                .requires(Requires.isMember())
                .requires(Requires.hasPerms("factions.deposit", 0))
                .executes(this::run)
                .build();
    }
}

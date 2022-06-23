package io.icker.factions.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.icker.factions.core.FactionStorage
import io.icker.factions.util.Command
import io.icker.factions.util.Message
import net.minecraft.block.Blocks
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

class StorageCommand : Command {
    private fun toggleChestMode(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val player = source.player
        val world = source.world

        val pos = BlockPosArgumentType.getBlockPos(context, "pos")
        val block = world.getBlockState(pos)
        if (!block.isOf(Blocks.ENDER_CHEST)) {
            Message("Target block must be an ender chest.").send(player, false)
            return 0
        }

        FactionStorage.toggleFactionEnderChest(pos)

        return 1
    }

    override fun getNode(): LiteralCommandNode<ServerCommandSource> {
        return CommandManager
            .literal("toggleChestMode")
            .then(
                CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                    .executes(this::toggleChestMode)
            )
            .build()
    }
}
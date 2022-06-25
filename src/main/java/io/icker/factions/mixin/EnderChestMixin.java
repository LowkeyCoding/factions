package io.icker.factions.mixin;

import io.icker.factions.core.FactionStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnderChestBlock.class)
public class EnderChestMixin {

    @ModifyVariable(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getBlockEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/entity/BlockEntity;"
    ), method = "onUse", name = "enderChestInventory")
    public EnderChestInventory openFactionChest(EnderChestInventory value, BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!FactionStorage.Companion.isFactionEnderChest(pos))
            return value;

        EnderChestInventory factionInventory = FactionStorage.Companion.tryOpenFactionEnderChest(player);
        return factionInventory != null ? factionInventory : value;
    }

}

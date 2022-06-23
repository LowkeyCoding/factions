package io.icker.factions.mixin;

import io.icker.factions.core.FactionStorage;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnderChestBlock.class)
public class EnderChestMixin {

    @Inject(at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/entity/player/PlayerEntity;getEnderChestInventory()Lnet/minecraft/inventory/EnderChestInventory;"
    ), method = "onUse", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void openFactionChest(
            BlockState state, World world,
            BlockPos pos, PlayerEntity player,
            Hand hand, BlockHitResult hit,
            CallbackInfoReturnable<ActionResult> cir,
            EnderChestInventory enderChestInventory
    ) {
        if (!FactionStorage.Companion.isFactionEnderChest(pos))
            return;

        EnderChestInventory factionInventory = FactionStorage.Companion.tryOpenFactionEnderChest(player);
        if (factionInventory != null)
            enderChestInventory = factionInventory;

    }

}

package io.icker.factions.mixin;

import io.icker.factions.api.events.PlayerEvents;
import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import io.icker.factions.api.persistents.User.ChatMode;
import io.icker.factions.core.InteractionsUtil;
import io.icker.factions.util.Message;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onPlayerMove", at = @At("HEAD"))
    public void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        PlayerEvents.ON_MOVE.invoker().onMove(player);
    }
    @Inject(method = "onPlayerInteractEntity", at = @At("HEAD"), cancellable = true)
    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci){
        ServerWorld world = player.getWorld();
        ActionResult result = UseEntityCallback.EVENT.invoker().interact(player, world, player.getActiveHand(), packet.getEntity(world), null);
        if(result != ActionResult.PASS) {
            InteractionsUtil.warn(player, "interact with entities");
            ci.cancel();
        }
    }

    @Redirect(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/server/filter/FilteredMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/util/registry/RegistryKey;)V"))
    private void replaceChatMessage(PlayerManager instance, FilteredMessage<SignedMessage> message, ServerPlayerEntity sender, RegistryKey<MessageType> typeKey) {
        User member = User.get(sender.getUuid());
        Faction faction = member != null ? member.getFaction() : null;

        boolean factionChat = member.getChatMode() == ChatMode.FACTION || member.getChatMode() == ChatMode.FOCUS;

        if (factionChat && faction == null) {
            new Message("You can't send a message to faction chat if you aren't in a faction").fail().hover("Click to switch to global chat").click("/factions chat global").send(sender, false);
        } else {
            instance.broadcast(message.raw(), (player) -> {
                User targetMember = User.get(player.getUuid());
                Faction target = targetMember != null ? targetMember.getFaction() : null;

                if (member.getChatMode() == ChatMode.GLOBAL && targetMember.getChatMode() != ChatMode.FOCUS) {
                    return message.getFilterableFor(sender, player);
                }

                if (factionChat && target != null && target.getName().equals(faction.getName())) {
                    return message.getFilterableFor(sender, player);
                }

                return null;
            }, sender.asMessageSender(), typeKey);
        }
    }
}

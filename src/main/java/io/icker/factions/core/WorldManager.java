package io.icker.factions.core;

import io.icker.factions.FactionsMod;
import io.icker.factions.api.events.MiscEvents;
import io.icker.factions.api.events.PlayerEvents;
import io.icker.factions.api.persistents.Claim;
import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import io.icker.factions.util.Message;
import io.icker.factions.util.SubChunk;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkPos;

import javax.security.auth.Subject;
import java.util.ArrayList;
import java.util.List;


public class WorldManager {
    public static void register() {
        PlayerEvents.ON_MOVE.register(WorldManager::onMove);
        MiscEvents.ON_MOB_SPAWN_ATTEMPT.register(WorldManager::onMobSpawnAttempt);
    }

    private static void onMobSpawnAttempt() {
        // TODO Implement this
    }
 
    private static void onMove(ServerPlayerEntity player) {
        User user = User.get(player.getUuid());
        ServerWorld world = player.getWorld();
        String dimension = world.getRegistryKey().getValue().toString();

        ChunkPos chunkPos = world.getChunk(player.getBlockPos()).getPos();

        if (user.getAutoclaim()) {
            Faction faction = user.getFaction();
            if(faction.getPower() < faction.requiredPower(1)){
                new Message("Not enough faction power to claim chunk, autoclaim toggled off").fail().send(player, false);
                user.toggleAutoclaim();
            } else {
                int lowerBound = faction.getVerticalRange().getX();
                int upperBound = faction.getVerticalRange().getY();
                ArrayList<SubChunk> chunks = new ArrayList<>();
                boolean isClaimed = false;

                for(int y = lowerBound; y < upperBound; y++){
                    Claim claim = Claim.get(chunkPos.x, y, chunkPos.z, dimension);
                    isClaimed = claim != null;
                    if(isClaimed && claim.getFaction().getID() == faction.getID()){
                        isClaimed = false;
                    } else {
                        chunks.add(new SubChunk(chunkPos.x, y, chunkPos.z, dimension));
                    }
                }

                if (!isClaimed) {
                    if(chunks.size() > 0){
                        chunks.forEach(c -> faction.addClaim(c.getX(), c.getY(), c.getZ(), c.getLevel()));
                        new Message("Chunk (%d, %d, %d) - (%d, %d, %d) claimed by %s",
                                chunkPos.x, lowerBound, chunkPos.z, chunkPos.x, upperBound,
                                chunkPos.z, player.getName().getString())
                                .send(faction);
                    }
                } else {
                    new Message("Chunk already claimed").fail().send(player, false);
                    user.toggleAutoclaim();
                }
            }
        }
        if (FactionsMod.CONFIG.RADAR && user.isRadarOn()) {
            int YPos = (int)Math.floor((double)player.getBlockY()/16);
            Claim claim = Claim.get(chunkPos.x, YPos, chunkPos.z, dimension);
            if (claim != null) {
                new Message(claim.getFaction().getName())
                        .format(claim.getFaction().getColor())
                        .send(player, true);
            } else {
                new Message("Wilderness")
                        .format(Formatting.GREEN)
                        .send(player, true);
            }
        }
    }
}

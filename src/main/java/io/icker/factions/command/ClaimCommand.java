package io.icker.factions.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.icker.factions.api.persistents.Claim;
import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import io.icker.factions.util.Command;
import io.icker.factions.util.Message;
import io.icker.factions.util.SubChunk;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClaimCommand implements Command {
    private int list(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        List<Claim> claims = User.get(player.getUuid()).getFaction().getClaims();
        int count = claims.size();

        new Message("You have ")
                .add(new Message(String.valueOf(count)).format(Formatting.YELLOW))
                .add(" claim%s", count == 1 ? "" : "s")
                .send(source.getPlayer(), false);

        if (count == 0) return 1;

        HashMap<String, ArrayList<Claim>> claimsMap = new HashMap<String, ArrayList<Claim>>();

        claims.forEach(claim -> {
            claimsMap.putIfAbsent(claim.level, new ArrayList<Claim>());
            claimsMap.get(claim.level).add(claim);
        });

        Message claimText = new Message("");
        claimsMap.forEach((level, array) -> {
            level = Pattern.compile("_([a-z])")
                    .matcher(level.split(":", 2)[1])
                    .replaceAll(m -> " " + m.group(1).toUpperCase());
            level = level.substring(0, 1).toUpperCase() +
                    level.substring(1);
            claimText.add("\n");
            claimText.add(new Message(level).format(Formatting.GRAY));
            claimText.filler("»");
            claimText.add(array.stream()
                    .map(claim -> String.format("(%d,%d)", claim.x, claim.z))
                    .collect(Collectors.joining(", ")));
        });

        claimText.format(Formatting.ITALIC).send(source.getPlayer(), false);
        return 1;
    }

    private int addForced(CommandContext<ServerCommandSource> context, int size) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = player.getWorld();

        Faction faction = User.get(player.getUuid()).getFaction();
        String dimension = world.getRegistryKey().getValue().toString();
        ArrayList<SubChunk> chunks = new ArrayList<SubChunk>();
        int lowerBound = faction.getVerticalRange().getX();
        int upperBound = faction.getVerticalRange().getY();
        if(lowerBound == upperBound) {
            new Message("Set player VerticalClaimRange using \"/f modify VerticalClaimRange lower upper\"").fail().send(player, false);
            return 0;
        }

        for (int x = -size+1; x <= size; x++) {
            for(int y = lowerBound;  y <= upperBound; y++) {
                for (int z = -size+1; z <= size; z++) {
                    ChunkPos chunkPos = world.getChunk(player.getBlockPos().add(x * 16, 0, z * 16)).getPos();
                    Claim existingClaim = Claim.get(chunkPos.x, y, chunkPos.z, dimension);

                    if (existingClaim != null) {
                        if (size == 1) {
                            String owner = existingClaim.getFaction().getID() == faction.getID() ? "Your" : "Another";
                            new Message(owner + " faction already owns this chunk").fail().send(player, false);
                            return 0;
                        } else if (existingClaim.getFaction().getID() != faction.getID()) {
                            new Message("Another faction already owns a chunk").fail().send(player, false);
                            return 0;
                        }
                    }

                    chunks.add(new SubChunk(chunkPos.x, y, chunkPos.z, dimension));
                }
            }
        }

        chunks.forEach(chunk -> faction.addClaim(chunk.getX(), chunk.getY(), chunk.getZ(), chunk.getLevel()));
        if (size == 1) {
            new Message("Chunk (%d, %d, %d) claimed by %s", chunks.get(0).getX(), chunks.get(0).getY(), chunks.get(0).getZ(), player.getName().getString())
                .send(faction);
        } else {
            new Message("Chunks (%d, %d, %d) to (%d, %d, %d) claimed by %s", chunks.get(0).getX(), chunks.get(0).getY(), chunks.get(0).getZ(),
                    chunks.get(chunks.size()-1).getX()+size, chunks.get(chunks.size()-1).getY(), chunks.get(chunks.size()-1).getZ()+size, player.getName().getString())
                .send(faction);
        }

        return 1;
    }

    private int add(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Faction faction = User.get(player.getUuid()).getFaction();

        if (faction.getPower() < faction.requiredPower(1)) {
            new Message("Not enough faction power to claim chunk").fail().send(player, false);
            return 0;
        }

        return addForced(context, 1);
    }

    private int addSize(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int size = IntegerArgumentType.getInteger(context, "size");
        ServerPlayerEntity player = context.getSource().getPlayer();
        Faction faction = User.get(player.getUuid()).getFaction();

        if (faction.getPower() < faction.requiredPower((int) Math.pow(size,4))) {
            new Message("Not enough faction power to claim chunks").fail().send(player, false);
            return 0;
        }

        return addForced(context, size);
    }

    private int remove(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = player.getWorld();
        User user = User.get(player.getUuid());
        Faction faction = user.getFaction();

        ChunkPos chunkPos = world.getChunk(player.getBlockPos()).getPos();
        String dimension = world.getRegistryKey().getValue().toString();

        int lowerBound = faction.getVerticalRange().getX();
        int upperBound = faction.getVerticalRange().getY();
        if(lowerBound == upperBound) {
            new Message("Set player VerticalClaimRange using \"/f modify VerticalClaimRange lower upper\"").fail().send(player, false);
            return 0;
        }
        for(int y = lowerBound;  y <= upperBound; y++) {
            Claim existingClaim = Claim.get(chunkPos.x, y, chunkPos.z, dimension);
            if (existingClaim == null) {
                new Message("Cannot remove a claim on an unclaimed chunk").fail().send(player, false);
                return 0;
            }


            if (!user.isBypassOn() && existingClaim.getFaction().getID() != faction.getID()) {
                new Message("Cannot remove a claim owned by another faction").fail().send(player, false);
                return 0;
            }

            existingClaim.remove();
            new Message("Claim (%d, %d) removed by %s", existingClaim.x, existingClaim.z, player.getName().getString()).send(faction);
        }
        return 1;

    }

    private int removeSize(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int size = IntegerArgumentType.getInteger(context, "size");
        ServerCommandSource source = context.getSource();

        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = player.getWorld();
        String dimension = world.getRegistryKey().getValue().toString();

        User user = User.get(player.getUuid());
        Faction faction = user.getFaction();

        int lowerBound = faction.getVerticalRange().getX();
        int upperBound = faction.getVerticalRange().getY();
        if(lowerBound == upperBound) {
            new Message("Set player VerticalClaimRange using \"/f modify VerticalClaimRange lower upper\"").fail().send(player, false);
            return 0;
        }

        for (int x = -size + 1; x < size; x++) {
            for(int y = lowerBound;  y <= upperBound; y++) {
                for (int z = -size + 1; z < size; z++) {
                    ChunkPos chunkPos = world.getChunk(player.getBlockPos().add(x * 16, 0, z * 16)).getPos();
                    Claim existingClaim = Claim.get(chunkPos.x, y, chunkPos.z, dimension);

                    if (existingClaim != null && (user.isBypassOn() || existingClaim.getFaction().getID() == faction.getID()))
                        existingClaim.remove();
                }
            }
        }

        ChunkPos chunkPos = world.getChunk(player.getBlockPos().add((-size + 1) * 16, 0, (-size + 1) * 16)).getPos();
        new Message("Claims (%d, %d) to (%d, %d) removed by %s ", chunkPos.x, chunkPos.z,
                chunkPos.x + size - 1, chunkPos.z + size - 1, player.getName().getString())
            .send(faction);

        return 1;
    }

    private int removeAll(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Faction faction = User.get(player.getUuid()).getFaction();

        faction.removeAllClaims();
        new Message("All claims removed by %s", player.getName().getString()).send(faction);
        return 1;
    }

    private int auto(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        User user = User.get(player.getUuid());
        user.toggleAutoclaim();

        new Message("Autoclaim toggled " + (user.getAutoclaim() ? "on" : "off")).send(player, false);
        return 1;
    }

    @Override
    public LiteralCommandNode<ServerCommandSource> getNode() {
        return CommandManager
            .literal("claim")
            .requires(Requires.isCommander())
            .then(
                CommandManager.literal("add")
                .requires(Requires.hasPerms("factions.claim.add", 0))
                .then(
                    CommandManager.argument("size", IntegerArgumentType.integer(1, 7))
                    .requires(Requires.hasPerms("factions.claim.add.size", 0))
                    .then(
                        CommandManager.literal("force")
                        .requires(Requires.hasPerms("factions.claim.add.force", 0))
                        .executes((context) -> addForced(context, IntegerArgumentType.getInteger(context, "size")))
                    )
                    .executes(this::addSize)
                )
                .executes(this::add)
            )
            .then(
                CommandManager.literal("list")
                .requires(Requires.hasPerms("factions.claim.list", 0))
                .executes(this::list)
            )
            .then(
                CommandManager.literal("remove")
                .requires(Requires.hasPerms("factions.claim.remove", 0))
                .then(
                    CommandManager.argument("size", IntegerArgumentType.integer(1, 7))
                    .requires(Requires.hasPerms("factions.claim.remove.size", 0))
                    .executes(this::removeSize)
                )
                .then(
                    CommandManager.literal("all")
                    .requires(Requires.hasPerms("factions.claim.remove.all", 0))
                    .executes(this::removeAll)
                )
                .executes(this::remove)
            )
            .then(
                CommandManager.literal("auto")
                .requires(Requires.hasPerms("factions.claim.auto", 0))
                .executes(this::auto)
            )
            .build();
    }
}
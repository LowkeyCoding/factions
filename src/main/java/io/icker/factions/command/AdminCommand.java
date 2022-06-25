package io.icker.factions.command;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.icker.factions.FactionsMod;
import io.icker.factions.api.persistents.Faction;
import io.icker.factions.api.persistents.User;
import io.icker.factions.config.Config;
import io.icker.factions.util.Command;
import io.icker.factions.util.Message;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

public class AdminCommand implements Command {
    private int bypass(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        User user = User.get(player.getUuid());
        boolean bypass = !user.isBypassOn();
        user.setBypass(bypass);

        new Message("Successfully toggled claim bypass")
                .filler("·")
                .add(
                        new Message(user.isBypassOn() ? "ON" : "OFF")
                                .format(user.isBypassOn() ? Formatting.GREEN : Formatting.RED))
                .send(player, false);

        return 1;
    }

    private int reload(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        FactionsMod.dynmap.reloadAll();
        new Message("Reloaded dynmap marker").send(context.getSource().getPlayer(), false);
        return 1;
    }

    private int disband(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Faction target = Faction.getByName(StringArgumentType.getString(context, "faction"));

        new Message("An admin disbanded the faction").send(target);
        target.remove();

        new Message("Faction has been removed").send(player, false);

        PlayerManager manager = source.getServer().getPlayerManager();
        for (ServerPlayerEntity p : manager.getPlayerList()) {
            manager.sendCommandTree(p);
        }
        return 1;
    }

    private LiteralArgumentBuilder<ServerCommandSource> configCommandBuilder(LiteralArgumentBuilder<ServerCommandSource> builder){
            var fields = Config.class.getDeclaredFields();
            for(int i = 3; i < fields.length; i++){
                var field = fields[i];
                var type = field.getType().getSimpleName();
                var argumentType = this.getArgumentType(type);
                var command = CommandManager.literal(field.getName())
                    .then(
                        CommandManager
                            .argument(type, argumentType)
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();
                                ServerPlayerEntity player = source.getPlayer();
                                var oldValue = "";
                                try {
                                    Object value;
                                    if(type.equals("HomeOptions")){
                                        value = context.getArgument(type, String.class);
                                        oldValue = field.get(FactionsMod.CONFIG).toString();
                                        field.set(FactionsMod.CONFIG,Config.HomeOptions.valueOf((String) value));
                                    } else if(type.equals("InteractionModes")){
                                        value = context.getArgument(type, String.class);
                                        oldValue = field.get(FactionsMod.CONFIG).toString();
                                        field.set(FactionsMod.CONFIG, Config.InteractionModes.valueOf((String) value));
                                    } else {
                                        value = context.getArgument(type, field.getType());
                                        oldValue = field.get(FactionsMod.CONFIG).toString();
                                        field.set(FactionsMod.CONFIG, value);
                                    }
                                    FactionsMod.CONFIG.save();
                                    new Message("Config " + field.getName() + ": " + Formatting.BOLD + oldValue +"→" + value).success().send(player, false);
                                } catch (IllegalArgumentException | IllegalAccessException e) {
                                    new Message(e.getMessage()).fail().send(player, false);
                                }
                                return 1;
                            })
                    );
                builder.then(command.executes(context -> {
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity player = source.getPlayer();
                    try {
                        new Message("Config " + field.getName() + ": " + Formatting.BOLD + field.get(FactionsMod.CONFIG)).send(player, false);
                    } catch (IllegalAccessException e) {
                        new Message(e.getMessage()).fail().send(player, false);
                    }
                    return 1;
                }));
            }
            return builder;
    }

    private ArgumentType<? extends java.io.Serializable> getArgumentType(String type){
        if(type.equals("int"))
            return IntegerArgumentType.integer();
        if(type.equals("float"))
            return FloatArgumentType.floatArg();
        if(type.equals("boolean"))
            return BoolArgumentType.bool();
        return StringArgumentType.string();
    }

    public LiteralCommandNode<ServerCommandSource> getNode() {
        return CommandManager
            .literal("admin")
            .then(
                CommandManager.literal("bypass")
                .requires(Requires.hasPerms("factions.admin.bypass", FactionsMod.CONFIG.REQUIRED_BYPASS_LEVEL))
                .executes(this::bypass)
            )
            .then(
                CommandManager.literal("reload")
                .requires(source -> FactionsMod.dynmap != null)
                .requires(Requires.hasPerms("factions.admin.reload", FactionsMod.CONFIG.REQUIRED_BYPASS_LEVEL))
                .executes(this::reload)
            )
            .then(
                CommandManager.literal("disband")
                .requires(Requires.hasPerms("factions.admin.disband", FactionsMod.CONFIG.REQUIRED_BYPASS_LEVEL))
                .then(
                    CommandManager.argument("faction", StringArgumentType.greedyString())
                    .suggests(Suggests.allFactions())
                    .executes(this::disband)
                )
            )
            .then(
                configCommandBuilder(CommandManager.literal("config")).build()
            )
            .build();
    }
}

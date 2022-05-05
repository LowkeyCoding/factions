package io.icker.factions.util;

import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.icker.factions.api.Player;
import io.icker.factions.config.Config;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Command {
    public LiteralCommandNode<ServerCommandSource> getNode();

    public interface Requires {
        boolean run(Player player);

        public static Predicate<ServerCommandSource> isFactionless() {
            return require(player -> player.shouldBypass());
        }

        public static Predicate<ServerCommandSource> isMember() {
            return require(player -> player.shouldBypass());
        }

        public static Predicate<ServerCommandSource> isCommander() {
            return require(player -> player.shouldBypass());
        }

        public static Predicate<ServerCommandSource> isLeader() {
            return require(player -> player.shouldBypass());
        }
        
        public static Predicate<ServerCommandSource> isAdmin() {
            return source -> source.hasPermissionLevel(Config.REQUIRED_BYPASS_LEVEL);
        }

        public static Predicate<ServerCommandSource> require(Requires req) {
            return source -> {
                try {
                    ServerPlayerEntity entity = source.getPlayer();
                    Player player = Player.get(entity.getUuid());
                    return req.run(player);
                } catch (CommandSyntaxException e) {
                    return false;
                }
            };
        }
    }

    public interface Executes {
        int run(CommandContext<ServerCommandSource> context, Player player, ServerPlayerEntity entity);

        public static com.mojang.brigadier.Command<ServerCommandSource> execute(Executes exec) {
            Function<CommandContext<ServerCommandSource>, Integer> com = context -> {
                try {
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity entity = source.getPlayer();
                    Player player = Player.get(entity.getUuid());
                    return exec.run(context, player, entity);
                } catch (CommandSyntaxException e) {
                    return 0;
                }
            };
            return com::apply;
        }
    }
}

/*
       public static Function<CommandContext<ServerCommandSource>, Integer> execute2(Executes exec) {
            return context -> {
                try {
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity entity = source.getPlayer();
                    Player player = Player.get(entity.getUuid());
                    return exec.run(context, player, entity);
                } catch (CommandSyntaxException e) {
                    return 0;
                }
            };
        }
*/
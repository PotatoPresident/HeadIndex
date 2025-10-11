package us.potatoboy.headindex.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import us.potatoboy.headindex.BuildableCommand;
import us.potatoboy.headindex.HeadIndex;

public class PlayerCommand implements BuildableCommand {
    @Override
    public LiteralCommandNode<ServerCommandSource> build() {
        return CommandManager.literal("player")
                .requires(Permissions.require("headindex.playername", HeadIndex.config.permissionLevel))
                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .executes(PlayerCommand::getPlayer))
                .build();
    }

    private static int getPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var profiles = GameProfileArgumentType.getProfileArgument(context, "player");
        if (profiles.size() != 1) {
            throw GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION.create();
        }
        var profile = profiles.iterator().next();
        var sessionService = context.getSource().getServer().getApiServices().sessionService();
        var result = sessionService.fetchProfile(profile.id(), false);

        if (result == null) {
            throw GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION.create();
        }

        var stack = Items.PLAYER_HEAD.getDefaultStack();
        stack.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(result.profile()));
        context.getSource().getPlayerOrThrow().getInventory().offerOrDrop(stack);
        return 1;
    }
}

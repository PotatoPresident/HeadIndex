package us.potatoboy.headindex.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import us.potatoboy.headindex.BuildableCommand;
import us.potatoboy.headindex.commands.HIPermissions;

public class PlayerCommand implements BuildableCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("player")
                .requires(HIPermissions.COMMAND_PLAYER)
                .then(Commands.argument("player", GameProfileArgument.gameProfile())
                        .executes(PlayerCommand::getPlayer))
                .build();
    }

    private static int getPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var profiles = GameProfileArgument.getGameProfiles(context, "player");
        if (profiles.size() != 1) {
            throw GameProfileArgument.ERROR_UNKNOWN_PLAYER.create();
        }
        var profile = profiles.iterator().next();
        var sessionService = context.getSource().getServer().services().sessionService();
        var result = sessionService.fetchProfile(profile.id(), false);

        if (result == null) {
            throw GameProfileArgument.ERROR_UNKNOWN_PLAYER.create();
        }

        var stack = Items.PLAYER_HEAD.getDefaultInstance();
        stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(result.profile()));
        context.getSource().getPlayerOrException().getInventory().placeItemBackInInventory(stack);
        return 1;
    }
}

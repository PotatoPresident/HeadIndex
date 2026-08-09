package us.potatoboy.headindex.commands.subcommands;

import com.mojang.authlib.GameProfile;
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
import us.potatoboy.headindex.api.GeyserHeadDatabaseAPI;
import us.potatoboy.headindex.commands.HIPermissions;

import java.util.Collection;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.players.NameAndId;

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
        boolean hasFloodgate = FabricLoader.getInstance().isModLoaded("floodgate");
        Collection<NameAndId> profiles = null;
        try {
            profiles = GameProfileArgument.getGameProfiles(context, "player");
        } catch (CommandSyntaxException e) {
            // Check if this is a Bedrock player name. If the player is offline no 
            // bedrock player profile is available in the argument parser.

            if (hasFloodgate) {
                String fullInput = context.getInput();
                String playerName = fullInput.substring(fullInput.lastIndexOf(' ')+1);
                profiles = GeyserHeadDatabaseAPI.getProfilesFromPlayerName(playerName);
            } else {
                throw GameProfileArgument.ERROR_UNKNOWN_PLAYER.create();
            }
        }
        if (profiles.size() != 1) {
            throw GameProfileArgument.ERROR_UNKNOWN_PLAYER.create();
        }
        var profile = profiles.iterator().next();

        var sessionService = context.getSource().getServer().services().sessionService();
        var result = sessionService.fetchProfile(profile.id(), false);

        GameProfile component;
        if (result != null) {
            component = result.profile();
        } else if (hasFloodgate) {
            // Check if this was an online or recently online bedrock player
            component = GeyserHeadDatabaseAPI.getGameProfileByUuidName(profile.id(), profile.name());
        } else {
            component = null;
        }

        if (component == null) {
           throw GameProfileArgument.ERROR_UNKNOWN_PLAYER.create();
        }

        var stack = Items.PLAYER_HEAD.getDefaultInstance();
        stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(component));
        context.getSource().getPlayerOrException().getInventory().placeItemBackInInventory(stack);
        return 1;
    }
}

package us.potatoboy.headindex.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import us.potatoboy.headindex.BuildableCommand;
import us.potatoboy.headindex.HeadIndex;
import us.potatoboy.headindex.gui.HeadGui;

public class MenuCommand implements BuildableCommand {
    @Override
    public LiteralCommandNode<ServerCommandSource> build() {
        return CommandManager.literal("menu")
                .requires(Permissions.require("headindex.menu", HeadIndex.config.permissionLevel))
                .executes(MenuCommand::openMenu)
                .build();
    }

    public static int openMenu(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        new HeadGui(context.getSource().getPlayerOrThrow()).open();
        
        if (HeadIndex.config.demoMode()) {
            context.getSource().sendError(HeadIndex.licenseWarn());
        }

        return 1;
    }
}

package us.potatoboy.headindex.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import us.potatoboy.headindex.HeadIndex;
import us.potatoboy.headindex.commands.subcommands.MenuCommand;
import us.potatoboy.headindex.commands.subcommands.SearchCommand;

public class HeadCommand {
    public HeadCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> root = CommandManager
                .literal("head")
                .requires(Permissions.require("headindex.menu", HeadIndex.config.permissionLevel))
                .executes(MenuCommand::openMenu)
                .build();

        dispatcher.getRoot().addChild(root);

        root.addChild(new MenuCommand().build());
        root.addChild(new SearchCommand().build());
    }
}

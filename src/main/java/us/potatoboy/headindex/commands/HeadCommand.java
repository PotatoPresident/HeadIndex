package us.potatoboy.headindex.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import us.potatoboy.headindex.commands.subcommands.MenuCommand;

public class HeadCommand {
    public HeadCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> root = CommandManager
                .literal("head")
                .executes(MenuCommand::openMenu)
                .build();

        dispatcher.getRoot().addChild(root);

        root.addChild(new MenuCommand().build());
    }
}

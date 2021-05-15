package us.potatoboy.headindex.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import us.potatoboy.headindex.BuildableCommand;
import us.potatoboy.headindex.gui.HeadGui;

public class MenuCommand implements BuildableCommand {
    @Override
    public LiteralCommandNode<ServerCommandSource> build() {
        return CommandManager.literal("menu")
                .executes(MenuCommand::openMenu)
                .build();
    }

    public static int openMenu(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        new HeadGui(context.getSource().getPlayer()).open();

        return 1;
    }
}

package us.potatoboy.headindex.commands.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import us.potatoboy.headindex.BuildableCommand;
import us.potatoboy.headindex.gui.HeadGui;

public class SearchCommand implements BuildableCommand {
    @Override
    public LiteralCommandNode<ServerCommandSource> build() {
        return CommandManager.literal("search")
                .then(CommandManager.argument("term", StringArgumentType.word())
                        .executes(SearchCommand::openSearch))
                .build();
    }

    public static int openSearch(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        new HeadGui(context.getSource().getPlayer()).openSearch(StringArgumentType.getString(context, "term"));

        return 1;
    }
}

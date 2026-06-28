package us.potatoboy.headindex.commands.subcommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import us.potatoboy.headindex.BuildableCommand;
import us.potatoboy.headindex.commands.HIPermissions;
import us.potatoboy.headindex.gui.HeadGui;

public class SearchCommand implements BuildableCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("search")
                .requires(HIPermissions.COMMAND_SEARCH)
                .then(Commands.argument("term", StringArgumentType.word())
                        .executes(SearchCommand::openSearch))
                .build();
    }

    public static int openSearch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        new HeadGui(context.getSource().getPlayerOrException()).openSearch(StringArgumentType.getString(context, "term"));

        return 1;
    }
}

package us.potatoboy.headindex.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import us.potatoboy.headindex.HeadIndex;
import us.potatoboy.headindex.commands.subcommands.MenuCommand;
import us.potatoboy.headindex.commands.subcommands.PlayerCommand;
import us.potatoboy.headindex.commands.subcommands.SearchCommand;

public class HeadCommand {
    public HeadCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> root = Commands
                .literal("head")
                .requires(Permissions.require("headindex.menu", HeadIndex.config.permissionLevel))
                .executes(MenuCommand::openMenu)
                .build();

        dispatcher.getRoot().addChild(root);

        root.addChild(new MenuCommand().build());
        root.addChild(new SearchCommand().build());
        root.addChild(new PlayerCommand().build());
    }
}

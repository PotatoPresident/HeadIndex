package us.potatoboy.headindex.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import us.potatoboy.headindex.BuildableCommand;
import us.potatoboy.headindex.HeadIndex;
import us.potatoboy.headindex.commands.HIPermissions;
import us.potatoboy.headindex.gui.HeadGuiFactory;

import net.fabricmc.loader.api.FabricLoader;

public class MenuCommand implements BuildableCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("menu")
                .requires(HIPermissions.COMMAND_MENU)
                .executes(MenuCommand::openMenu)
                .build();
    }

    public static int openMenu(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final boolean hasFloodgate = FabricLoader.getInstance().isModLoaded("floodgate");
        HeadGuiFactory.makeHeadGui(context.getSource().getPlayerOrException()).open();
        
        if (HeadIndex.config.demoMode()) {
            context.getSource().sendSystemMessage(HeadIndex.licenseWarn());
        }

        return 1;
    }
}

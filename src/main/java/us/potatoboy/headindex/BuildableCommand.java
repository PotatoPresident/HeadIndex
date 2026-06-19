package us.potatoboy.headindex;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;

public interface BuildableCommand {
    LiteralCommandNode<CommandSourceStack> build();
}

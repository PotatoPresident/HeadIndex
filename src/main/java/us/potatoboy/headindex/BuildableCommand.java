package us.potatoboy.headindex;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;

public interface BuildableCommand {
    LiteralCommandNode<ServerCommandSource> build();
}

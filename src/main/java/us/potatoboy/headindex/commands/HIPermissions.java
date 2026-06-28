package us.potatoboy.headindex.commands;

import net.fabricmc.fabric.api.permission.v1.PermissionPredicates;
import net.minecraft.commands.CommandSourceStack;
import us.potatoboy.headindex.HeadIndex;

import java.util.function.Predicate;

public class HIPermissions {
    public static final Predicate<CommandSourceStack> COMMAND_MENU = PermissionPredicates.require(
            HeadIndex.id("command.menu"), HeadIndex.config.getPermissionLevel());
    public static final Predicate<CommandSourceStack> COMMAND_PLAYER = PermissionPredicates.require(
            HeadIndex.id("command.player"), HeadIndex.config.getPermissionLevel());
    public static final Predicate<CommandSourceStack> COMMAND_SEARCH = PermissionPredicates.require(
            HeadIndex.id("command.search"), HeadIndex.config.getPermissionLevel());
}

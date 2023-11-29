package mods.thecomputerizer.scriptify.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public interface ISubType<T> {

    ISubType<T> collect(String ... args) throws CommandException;
    T execute(MinecraftServer server, ICommandSender sender) throws CommandException;
    String getLang(String ... args);
    String getName();
    List<String> getTabCompletions(String ... args);
    boolean isParameter();
    int isRequired();
}

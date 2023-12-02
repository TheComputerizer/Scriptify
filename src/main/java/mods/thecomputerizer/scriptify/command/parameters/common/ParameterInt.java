package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import mods.thecomputerizer.scriptify.command.parameters.Parser;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.List;

public class ParameterInt extends Parameter<Integer> {

    public ParameterInt(Type type) {
        super(type);
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        return Collections.emptyList();
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    protected Integer parse(MinecraftServer server, ICommandSender sender, String valueStr) throws CommandException {
        return Parser.parseInt(this,valueStr);
    }
}

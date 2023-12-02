package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.List;

public class ParameterString extends Parameter<String> {

    public ParameterString(Type type) {
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
    protected String parse(MinecraftServer server, ICommandSender sender, String valueStr) {
        return valueStr;
    }
}

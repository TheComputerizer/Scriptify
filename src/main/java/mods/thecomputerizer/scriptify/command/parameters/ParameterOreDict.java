package mods.thecomputerizer.scriptify.command.parameters;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.List;

public class ParameterOreDict extends Parameter<Integer[]> {

    public ParameterOreDict() {
        super(Type.PARAMETER_OREDICT);
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
    protected Integer[] parse(MinecraftServer server, ICommandSender sender, String valueStr) throws CommandException {
        return Parser.parseIntArray(this,valueStr);
    }
}

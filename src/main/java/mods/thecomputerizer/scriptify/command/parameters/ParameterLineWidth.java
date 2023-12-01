package mods.thecomputerizer.scriptify.command.parameters;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public class ParameterLineWidth extends Parameter<Integer> {

    public ParameterLineWidth() {
        super(Type.PARAMETER_MAX_LINE_WIDTH);
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        return null;
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

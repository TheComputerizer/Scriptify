package mods.thecomputerizer.scriptify.command.parameters;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;

import java.util.Collections;
import java.util.List;

public class ParameterContainerSize extends Parameter<Tuple<Integer,Integer>> {

    public ParameterContainerSize() {
        super(Type.PARAMETER_CONTAINER_SIZE);
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
    protected Tuple<Integer,Integer> parse(MinecraftServer server, ICommandSender sender, String valueStr) throws CommandException {
        return Parser.parseIntegerPair(this,valueStr);
    }
}

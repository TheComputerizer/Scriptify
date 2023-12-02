package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import mods.thecomputerizer.scriptify.command.parameters.Parser;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ParameterArray<T,P extends Parameter<T>> extends Parameter<List<T>> {

    private final P elementReference;

    public ParameterArray(Type type, Function<Type,P> referenceCreator) {
        super(type);
        this.elementReference = referenceCreator.apply(type);
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
    protected List<T> parse(MinecraftServer server, ICommandSender sender, String valueStr) throws CommandException {
        return Parser.parseArray(server,sender,this.elementReference,valueStr);
    }
}

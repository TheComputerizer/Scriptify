package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parser;
import net.minecraft.command.CommandException;

public class ParameterInt extends ParameterNumber<Integer> {

    public ParameterInt(Type type) {
        super(type);
    }

    @Override
    protected Integer parse(String valueStr) throws CommandException {
        return Parser.parseInt(this,valueStr);
    }
}

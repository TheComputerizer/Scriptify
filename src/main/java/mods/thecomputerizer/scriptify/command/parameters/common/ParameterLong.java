package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parser;
import net.minecraft.command.CommandException;

public class ParameterLong extends ParameterNumber<Long> {

    public ParameterLong(Type type) {
        super(type);
    }

    @Override
    protected Long parse(String valueStr) throws CommandException {
        return Parser.parseLong(this,valueStr);
    }
}

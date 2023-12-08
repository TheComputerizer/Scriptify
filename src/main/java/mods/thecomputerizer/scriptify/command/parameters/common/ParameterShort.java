package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parser;
import net.minecraft.command.CommandException;

public class ParameterShort extends ParameterNumber<Short> {

    public ParameterShort(Type type) {
        super(type);
    }

    @Override
    protected Short parse(String valueStr) throws CommandException {
        return Parser.parseShort(this,valueStr);
    }
}

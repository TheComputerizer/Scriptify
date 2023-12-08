package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parser;
import net.minecraft.command.CommandException;

public class ParameterByte extends ParameterNumber<Byte> {

    public ParameterByte(Type type) {
        super(type);
    }

    @Override
    protected Byte parse(String valueStr) throws CommandException {
        return Parser.parseByte(this,valueStr);
    }
}

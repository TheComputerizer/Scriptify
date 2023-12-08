package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parser;
import net.minecraft.command.CommandException;

public class ParameterFloat extends ParameterNumber<Float> {

    public ParameterFloat(Type type) {
        super(type);
    }

    @Override
    protected Float parse(String valueStr) throws CommandException {
        return Parser.parseFloat(this,valueStr);
    }
}

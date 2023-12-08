package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parser;
import net.minecraft.command.CommandException;

public class ParameterDouble extends ParameterNumber<Double> {

    public ParameterDouble(Type type) {
        super(type);
    }

    @Override
    protected Double parse(String valueStr) throws CommandException {
        return Parser.parseDouble(this,valueStr);
    }
}

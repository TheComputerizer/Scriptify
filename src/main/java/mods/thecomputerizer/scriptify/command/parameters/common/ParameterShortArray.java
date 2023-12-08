package mods.thecomputerizer.scriptify.command.parameters.common;

public class ParameterShortArray extends ParameterNumberArray<Short,ParameterShort> {

    public ParameterShortArray(Type type) {
        super(type,ParameterShort::new);
    }
}

package mods.thecomputerizer.scriptify.command.parameters.common;

public class ParameterDoubleArray extends ParameterNumberArray<Double,ParameterDouble> {

    public ParameterDoubleArray(Type type) {
        super(type,ParameterDouble::new);
    }
}

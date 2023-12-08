package mods.thecomputerizer.scriptify.command.parameters.common;

public class ParameterFloatArray extends ParameterNumberArray<Float,ParameterFloat> {
    protected ParameterFloatArray(Type type) {
        super(type,ParameterFloat::new);
    }
}

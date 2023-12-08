package mods.thecomputerizer.scriptify.command.parameters.common;

public class ParameterIntArray extends ParameterNumberArray<Integer,ParameterInt> {

    public ParameterIntArray(Type type) {
        super(type,ParameterInt::new);
    }
}

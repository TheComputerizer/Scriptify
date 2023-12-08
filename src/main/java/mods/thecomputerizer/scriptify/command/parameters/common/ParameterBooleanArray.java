package mods.thecomputerizer.scriptify.command.parameters.common;

public class ParameterBooleanArray extends ParameterArray<Boolean,ParameterBoolean> {

    public ParameterBooleanArray(Type type) {
        super(type,ParameterBoolean::new);
    }
}

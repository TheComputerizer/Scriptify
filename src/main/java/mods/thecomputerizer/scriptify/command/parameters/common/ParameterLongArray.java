package mods.thecomputerizer.scriptify.command.parameters.common;

public class ParameterLongArray extends ParameterNumberArray<Long,ParameterLong> {
    protected ParameterLongArray(Type type) {
        super(type,ParameterLong::new);
    }
}

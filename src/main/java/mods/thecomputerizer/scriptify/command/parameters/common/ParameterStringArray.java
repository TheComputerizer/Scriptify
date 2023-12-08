package mods.thecomputerizer.scriptify.command.parameters.common;

public class ParameterStringArray extends ParameterArray<String,ParameterString> {

    public ParameterStringArray(Type type) {
        super(type,ParameterString::new);
    }
}

package mods.thecomputerizer.scriptify.command.parameters.common;

public class ParameterByteArray extends ParameterNumberArray<Byte,ParameterByte> {

    public ParameterByteArray(Type type) {
        super(type,ParameterByte::new);
    }
}

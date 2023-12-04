package mods.thecomputerizer.scriptify.command.parameters.types;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterArray;
import mods.thecomputerizer.scriptify.command.parameters.common.ParameterString;

public class ParameterParameters extends ParameterArray<String,ParameterString> {

    public ParameterParameters() {
        super(Type.PARAMETER_PARAMETERS,ParameterString::new);
    }
}

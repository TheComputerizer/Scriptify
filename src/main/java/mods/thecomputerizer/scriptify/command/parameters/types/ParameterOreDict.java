package mods.thecomputerizer.scriptify.command.parameters.types;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterArray;
import mods.thecomputerizer.scriptify.command.parameters.common.ParameterInt;

public class ParameterOreDict extends ParameterArray<Integer,ParameterInt> {

    public ParameterOreDict() {
        super(Type.PARAMETER_OREDICT,ParameterInt::new);
    }
}

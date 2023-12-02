package mods.thecomputerizer.scriptify.command.parameters.types;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterArray;
import mods.thecomputerizer.scriptify.command.parameters.common.ParameterInt;

public class ParameterContainerSize extends ParameterArray<Integer,ParameterInt> {

    public ParameterContainerSize() {
        super(Type.PARAMETER_CONTAINER_SIZE,ParameterInt::new);
    }
}

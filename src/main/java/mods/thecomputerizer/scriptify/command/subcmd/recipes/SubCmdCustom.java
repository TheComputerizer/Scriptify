package mods.thecomputerizer.scriptify.command.subcmd.recipes;

import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;

public class SubCmdCustom extends SubCmd {
    public SubCmdCustom() {
        super(Type.CUSTOM, Parameter.Type.NAME.make());
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    protected boolean hasParameters() {
        return true;
    }
}

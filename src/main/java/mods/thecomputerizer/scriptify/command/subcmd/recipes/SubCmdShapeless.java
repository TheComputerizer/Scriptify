package mods.thecomputerizer.scriptify.command.subcmd.recipes;

import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;

public class SubCmdShapeless extends SubCmd {

    public SubCmdShapeless() {
        super(Type.SHAPELESS);
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

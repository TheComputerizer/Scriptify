package mods.thecomputerizer.scriptify.command.subcmd.recipes;

import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;

public class SubCmdShaped extends SubCmd {
    public SubCmdShaped() {
        super(Type.SHAPED);
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

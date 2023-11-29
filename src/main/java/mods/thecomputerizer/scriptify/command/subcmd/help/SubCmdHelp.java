package mods.thecomputerizer.scriptify.command.subcmd.help;

import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;

public class SubCmdHelp extends SubCmd {

    public SubCmdHelp() {
        super(Type.HELP, Type.COMMANDS.make(), Type.PARAMETERS.make());
    }
    @Override
    protected boolean hasParameters() {
        return false;
    }

    @Override
    public int isRequired() {
        return 0;
    }
}

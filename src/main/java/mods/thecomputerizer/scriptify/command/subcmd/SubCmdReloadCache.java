package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;

public class SubCmdReloadCache extends SubCmd {

    public SubCmdReloadCache() {
        super(Type.COMMAND_RELOAD_CACHE);
    }

    @Override
    public void execute() {
        ScriptifyConfigHelper.onConfigReloaded();
        sendGeneric(sender,array(this.getName(),"success"));
    }

    @Override
    protected void executeOnPacket(PacketSendContainerInfo packet) {}

    @Override
    protected boolean hasParameters() {
        return true;
    }

    @Override
    public int isRequired() {
        return 0;
    }
}

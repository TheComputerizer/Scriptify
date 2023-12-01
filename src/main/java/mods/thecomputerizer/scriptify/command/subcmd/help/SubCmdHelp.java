package mods.thecomputerizer.scriptify.command.subcmd.help;

import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public class SubCmdHelp extends SubCmd {

    public SubCmdHelp() {
        super(Type.COMMAND_HELP,Type.COMMAND_COMMANDS,Type.COMMAND_PARAMETERS);
    }
    @Override
    protected boolean hasParameters() {
        return false;
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    protected void executeOnPacket(MinecraftServer server, @Nullable EntityPlayerMP player, PacketSendContainerInfo packet) {}
}

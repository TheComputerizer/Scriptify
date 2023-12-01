package mods.thecomputerizer.scriptify.command.subcmd.help;

import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public class SubCmdParameters extends SubCmd {

    public SubCmdParameters() {
        super(Type.COMMAND_PARAMETERS);
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) {
        return this;
    }

    @Override
    public int isRequired() {
        return 2;
    }

    @Override
    protected boolean hasParameters() {
        return true;
    }

    @Override
    protected void executeOnPacket(MinecraftServer server, @Nullable EntityPlayerMP player, PacketSendContainerInfo packet) {}
}

package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public class SubCmdReloadCache extends SubCmd {

    public SubCmdReloadCache() {
        super(Type.COMMAND_RELOAD_CACHE);
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        ScriptifyConfigHelper.onConfigReloaded();
        sendGeneric(sender,array(this.getName(),"success"));
        return this;
    }

    @Override
    protected void executeOnPacket(MinecraftServer server, @Nullable EntityPlayerMP player, PacketSendContainerInfo packet) {

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

package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.Objects;

public class SubCmdRun extends SubCmd {

    public SubCmdRun() {
        super(Type.COMMAND_RUN,Type.PARAMETER_TYPE);
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        String command = ScriptifyConfigHelper.buildCommand((String)getParameter(this.getType(),"type").execute(server,sender));
        if(Objects.nonNull(command)) {
            server.commandManager.executeCommand(sender,command);
            sendSuccess(sender,command);
        } else throwGeneric(array(getName(),"fail"));
        return this;
    }

    @Override
    protected void executeOnPacket(MinecraftServer server, @Nullable EntityPlayerMP player, PacketSendContainerInfo packet) {

    }

    @Override
    protected boolean hasParameters() {
        return true;
    }
}

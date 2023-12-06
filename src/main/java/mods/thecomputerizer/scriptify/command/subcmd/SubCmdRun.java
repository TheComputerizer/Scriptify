package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("unchecked")
public class SubCmdRun extends SubCmd {

    public SubCmdRun() {
        super(Type.COMMAND_RUN,Type.PARAMETER_COMMANDS);
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        List<String> commandNames = (List<String>)getParameter(this.getType(),"commands").execute(server,sender);
        if(commandNames.isEmpty()) throwGeneric(array(getName(),"empty"));
        for(String commandName : commandNames) {
            String command = ScriptifyConfigHelper.buildCommand(commandName);
            if(StringUtils.isNotBlank(command)) {
                server.commandManager.executeCommand(sender, command);
                sendSuccess(sender, command);
            } else throwGeneric(array(getName(), "fail"),commandName);
        }
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

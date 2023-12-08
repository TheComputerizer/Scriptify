package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import net.minecraft.command.CommandException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SubCmdRun extends SubCmd {

    public SubCmdRun() {
        super(Type.COMMAND_RUN,Type.PARAMETER_COMMANDS);
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    public void execute() throws CommandException {
        List<String> commandNames = getParameterAsList("commands");
        if(commandNames.isEmpty()) throwGeneric(array(getName(),"empty"));
        for(String commandName : commandNames) {
            String command = ScriptifyConfigHelper.buildCommand(commandName);
            if(StringUtils.isNotBlank(command)) {
                server.commandManager.executeCommand(sender, command);
                sendSuccess(sender, command);
            } else throwGeneric(array(getName(), "fail"),commandName);
        }
    }

    @Override
    protected void executeOnPacket(PacketSendContainerInfo packet) {}

    @Override
    protected boolean hasParameters() {
        return true;
    }
}

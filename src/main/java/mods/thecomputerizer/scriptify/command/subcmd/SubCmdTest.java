package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.io.read.ZenFileReader;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public class SubCmdTest extends SubCmd {

    public SubCmdTest() {
        super(Type.COMMAND_TEST,Type.PARAMETER_PARAMETERS,Type.PARAMETER_ZEN_FILE_INPUT,
                Type.PARAMETER_ZEN_FILE_OUTPUT);
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        defineParameterSets(server,sender);
        String fileInput = ((String)getParameter("zenFileInput").execute(server,sender)).trim().toLowerCase();
        String fileOutput = ((String)getParameter("zenFileOutput").execute(server,sender)).trim().toLowerCase();
        new ZenFileReader(fileInput).testMove(fileOutput);
        sendGeneric(sender,array("test","move","success"),fileInput,fileOutput);
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

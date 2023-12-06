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
        super(Type.COMMAND_TEST,Type.PARAMETER_PARAMETERS,Type.PARAMETER_TYPE,Type.PARAMETER_SAVE_PARAMETERS,
                Type.PARAMETER_ZEN_FILE_INPUT,Type.PARAMETER_ZEN_FILE_OUTPUT);
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        defineParameterSets(server,sender);
        String type = ((String)getParameter(this.getType(),"type").execute(server,sender));
        String fileInput = ((String)getParameter(this.getType(),"zenFileInput").execute(server,sender)).toLowerCase();
        if(type.matches("move")) {
            String fileOutput = ((String) getParameter(this.getType(), "zenFileOutput").execute(server, sender)).toLowerCase();
            new ZenFileReader(fileInput).testMove(fileOutput);
            sendGeneric(sender,array("test","move","success"),fileInput,fileOutput);
        } else if(type.matches("read")) {
            new ZenFileReader(fileInput).tryParsingRecipeData();
            sendGeneric(sender,array("test","read","success"),fileInput);
        } else throwGeneric(array("test","fail"),type);
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

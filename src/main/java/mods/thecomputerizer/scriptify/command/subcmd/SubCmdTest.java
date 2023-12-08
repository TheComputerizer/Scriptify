package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.io.read.ZenFileReader;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraft.command.CommandException;

import java.util.List;

public class SubCmdTest extends SubCmd {

    public SubCmdTest() {
        super(Type.COMMAND_TEST,Type.PARAMETER_PARAMETERS,Type.PARAMETER_TYPE,Type.PARAMETER_SAVE_PARAMETERS,
                Type.PARAMETER_ZEN_FILE_INPUTS,Type.PARAMETER_ZEN_FILE_OUTPUTS);
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    public void execute() throws CommandException {
        defineParameterSets();
        String type = getParameterAsString("type");
        List<String> inputFilePaths = getParameterAsFileList("zenFileInput");
        if(type.matches("move")) {
            List<String> outputFilePaths = getParameterAsFileList("zenFileOutput");
            if(!inputFilePaths.isEmpty() && !outputFilePaths.isEmpty()) {
                String from = inputFilePaths.get(0);
                String to = outputFilePaths.get(0);
                new ZenFileReader(from).testMove(to);
                sendGeneric(sender,array("test","move","success"),from,to);
            }
        } else if(type.matches("read")) {
            for(String path : inputFilePaths) new ZenFileReader(path).tryParsingRecipeData();
            sendGeneric(sender,array("test","read","success"),TextUtil.listToString(inputFilePaths,","));
        } else throwGeneric(array("test","fail"),type);
    }

    @Override
    protected void executeOnPacket(PacketSendContainerInfo packet) {}

    @Override
    protected boolean hasParameters() {
        return true;
    }
}

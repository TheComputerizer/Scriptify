package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.io.write.ZenFileWriter;
import mods.thecomputerizer.scriptify.network.PacketQueryContainer;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;

import java.util.ArrayList;
import java.util.List;

public class SubCmdRecipe extends SubCmd {

    public SubCmdRecipe() {
        super(Type.COMMAND_RECIPE,Type.PARAMETER_CONTAINER_SIZE,Type.PARAMETER_CONTAINER_TYPE,Type.PARAMETER_NAME,
                Type.PARAMETER_PARAMETERS,Type.PARAMETER_SAVE_PARAMETERS,Type.PARAMETER_TOTAL_SLOTS,Type.PARAMETER_TYPE,
                Type.PARAMETER_ZEN_FILE_OUTPUTS);
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    protected boolean hasParameters() {
        return true;
    }

    @Override
    public void execute() throws CommandException {
        defineParameterSets();
        getParameterAsString("containerType");
        String containerType = getParameterAsString("containerType").toLowerCase();
        if(sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            if(containerType.matches("container")) new PacketQueryContainer(this).addPlayers(player).send();
        }
    }

    @Override
    protected void executeOnPacket(PacketSendContainerInfo packet) {
        if(this.sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)this.sender;
            String type = "unknown";
            List<String> filePaths = new ArrayList<>();
            try {
                defineParameterSets();
                type = getParameterAsString("type");
                IInventory inventory = packet.getInventory(player.getServerWorld());
                String name = getParameterAsString("name");
                filePaths = getParameterAsFileList("zenFileOutput");
                for(String path : filePaths) {
                    ZenFileWriter writer = shapelessTest(inventory,name,path);
                    writer.addPreProcessor("reloadable");
                    writer.writeToFile(path, true);
                }
                sendSuccess(player,this.getName());
            } catch(CommandException ex) {
                ScriptifyRef.LOGGER.error("Unable to execute command {} from packet!",this.getName(),ex);
                sendGeneric(player,array(this.getName(),"fail"),type,TextUtil.listToString(filePaths,","));
            }
        }
    }

    private ZenFileWriter shapelessTest(IInventory inventory, String name, String filePath) {
        ScriptifyRef.LOGGER.info("Attempting to write shapeless recipe with name {} from inventory {} to " +
                "file {}",name,inventory,filePath);
        //RecipeBlueprint data = RecipeDataHandler.CRAFTING_SHAPELESS_BLUEPRINT;
        //String recipeStr = data.write(name,inventory.getStackInSlot(3),inventory.getStackInSlot(0),
                //inventory.getStackInSlot(1),inventory.getStackInSlot(2));
        //SingletonWriter writer = new SingletonWriter(recipeStr);
        //writer.addComment("Automagically generated!");
        //ScriptifyRef.LOGGER.info("Writing recipe from string `{}`",recipeStr);
        return null;
    }
}

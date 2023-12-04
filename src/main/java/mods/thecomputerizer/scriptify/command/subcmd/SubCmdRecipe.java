package mods.thecomputerizer.scriptify.command.subcmd;

import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.data.RecipeBlueprint;
import mods.thecomputerizer.scriptify.data.RecipeDataHandler;
import mods.thecomputerizer.scriptify.io.write.SingletonWriter;
import mods.thecomputerizer.scriptify.network.PacketQueryContainer;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import mods.thecomputerizer.scriptify.io.write.ZenFileWriter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.Objects;

public class SubCmdRecipe extends SubCmd {

    public SubCmdRecipe() {
        super(Type.COMMAND_RECIPE,Type.PARAMETER_CONTAINER_SIZE,Type.PARAMETER_CONTAINER_TYPE,Type.PARAMETER_NAME,
                Type.PARAMETER_PARAMETERS, Type.PARAMETER_TOTAL_SLOTS,Type.PARAMETER_TYPE,
                Type.PARAMETER_ZEN_FILE_OUTPUT);
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
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        defineParameterSets(server,sender);
        String containerType = ((String)getParameter("containerType").execute(server,sender)).trim().toLowerCase();
        if(sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)sender;
            if(containerType.matches("container")) new PacketQueryContainer(this).addPlayers(player).send();
        }
        return this;
    }

    @Override
    protected void executeOnPacket(MinecraftServer server, @Nullable EntityPlayerMP player, PacketSendContainerInfo packet) {
        if(Objects.nonNull(player)) {
            String type = "unknown";
            String filePath = "unknown";
            try {
                defineParameterSets(server,player);
                type = (String)getParameter("type").execute(server,player);
                IInventory inventory = packet.getInventory(player.getServerWorld());
                String name = (String)getParameter("name").execute(server,player);
                filePath = (String)getParameter("zenFileOutput").execute(server,player);
                ZenFileWriter writer = shapelessTest(inventory,name,filePath);
                writer.addPreProcessor("reloadable");
                writer.write(filePath,true);
                sendSuccess(player,this.getName());
            } catch(CommandException ex) {
                ScriptifyRef.LOGGER.error("Unable to execute command {} from packet!",this.getName(),ex);
                sendGeneric(player,array(this.getName(),"fail"),type,filePath);
            }
        }
    }

    private ZenFileWriter shapelessTest(IInventory inventory, String name, String filePath) {
        ScriptifyRef.LOGGER.info("Attempting to write shapeless recipe with name {} from inventory {} to " +
                "file {}",name,inventory,filePath);
        RecipeBlueprint data = RecipeDataHandler.CRAFTING_SHAPELESS_BLUEPRINT;
        String recipeStr = data.write(name,inventory.getStackInSlot(3),inventory.getStackInSlot(0),
                inventory.getStackInSlot(1),inventory.getStackInSlot(2));
        SingletonWriter writer = new SingletonWriter(recipeStr);
        writer.addComment("Automagically generated!");
        ScriptifyRef.LOGGER.info("Writing recipe from string `{}`",recipeStr);
        return new ZenFileWriter(writer);
    }
}

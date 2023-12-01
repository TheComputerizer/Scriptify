package mods.thecomputerizer.scriptify.command.subcmd.recipes;

import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import mods.thecomputerizer.scriptify.data.RecipeData;
import mods.thecomputerizer.scriptify.data.ShapelessData;
import mods.thecomputerizer.scriptify.network.PacketQueryContainer;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import mods.thecomputerizer.scriptify.write.RecipeWriter;
import mods.thecomputerizer.scriptify.write.ZenFileWriter;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.Objects;

public class SubCmdShapeless extends SubCmd {

    public SubCmdShapeless() {
        super(Type.COMMAND_SHAPELESS,Type.PARAMETER_CONTAINER_SIZE,Type.PARAMETER_NAME,Type.PARAMETER_TOTAL_SLOTS,
                Type.PARAMETER_CONTAINER_TYPE,Type.PARAMETER_ZEN_FILE_OUTPUT);
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
        String containerType = (String)getParameter("containerType").execute(server,sender);
        if(containerType.trim().toLowerCase().matches("container") && sender instanceof EntityPlayerMP)
            new PacketQueryContainer(this).addPlayers((EntityPlayerMP)sender).send();
        return this;
    }

    @Override
    protected void executeOnPacket(MinecraftServer server, @Nullable EntityPlayerMP player, PacketSendContainerInfo packet) {
        if(Objects.nonNull(player)) {
            try {
                IInventory inventory = packet.getInventory(player.getServerWorld());
                String name = (String)getParameter("name").execute(server,player);
                String filePath = (String)getParameter("zenFileOutput").execute(server,player);
                ZenFileWriter writer = shapelessTest(inventory,name,filePath);
                writer.addPreProcessor("reloadable");
                writer.write(filePath,true);
            } catch(CommandException ex) {
                ScriptifyRef.LOGGER.error("Unable to execute command {} from packet!",this.getName(),ex);
            }
        }
    }

    private ZenFileWriter shapelessTest(IInventory inventory, String name, String filePath) {
        ScriptifyRef.LOGGER.info("Attempting to write shapeless recipe with name {} from inventory {} to " +
                "file {}",name,inventory,filePath);
        RecipeData data = new ShapelessData();
        data.setName(name);
        data.addInputs(inventory.getStackInSlot(0),inventory.getStackInSlot(1),inventory.getStackInSlot(2));
        data.setOutput(inventory.getStackInSlot(3));
        RecipeWriter writer = data.writeRecipe();
        writer.addComment("Automatically generated!");
        ScriptifyRef.LOGGER.info("Writing recipe from string `{}`",writer);
        return new ZenFileWriter(writer);
    }
}

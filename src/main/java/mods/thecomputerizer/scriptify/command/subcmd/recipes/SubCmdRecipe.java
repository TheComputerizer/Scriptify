package mods.thecomputerizer.scriptify.command.subcmd.recipes;

import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public class SubCmdRecipe extends SubCmd {

    public SubCmdRecipe() {
        super(Type.COMMAND_RECIPE,Type.COMMAND_CUSTOM,Type.COMMAND_SHAPED,Type.COMMAND_SHAPELESS);
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    protected boolean hasParameters() {
        return false;
    }

    @Override
    protected void executeOnPacket(MinecraftServer server, @Nullable EntityPlayerMP player, PacketSendContainerInfo packet) {}
}

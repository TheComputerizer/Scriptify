package mods.thecomputerizer.scriptify.client;

import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.util.Objects;

public class ScriptifyClient {

    public static void containerPos(SubCmd sub) {
        BlockPos pos = BlockPos.ORIGIN;
        boolean isBlock = false;
        RayTraceResult res = Minecraft.getMinecraft().objectMouseOver;
        if(Objects.nonNull(res) && res.typeOfHit==RayTraceResult.Type.BLOCK) {
            isBlock = true;
            pos = res.getBlockPos();
        }
        new PacketSendContainerInfo(sub,isBlock,pos).send();
    }
}

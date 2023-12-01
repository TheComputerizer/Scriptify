package mods.thecomputerizer.scriptify.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;

public class PacketSendContainerInfo extends MessageImpl {

    private SubCmd sub;
    private boolean isBlock;
    private BlockPos pos;

    public PacketSendContainerInfo() {}

    public PacketSendContainerInfo(SubCmd sub, boolean isBlock, BlockPos pos) {
        this.sub = sub;
        this.isBlock = isBlock;
        this.pos = pos;
    }

    @Override
    public IMessage handle(MessageContext ctx) {
        this.sub.handlePacket(this,ctx.getServerHandler().player);
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.sub = SubCmd.buildFromPacket(buf);
        this.isBlock = buf.readBoolean();
        this.pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        this.sub.send(buf);
        buf.writeBoolean(this.isBlock);
        buf.writeLong(this.pos.toLong());
    }

    public @Nullable IInventory getInventory(World world) {
        if(!this.isBlock) return null;
        TileEntity tile = world.getTileEntity(this.pos);
        return tile instanceof IInventory ? (IInventory)tile : null;
    }
}

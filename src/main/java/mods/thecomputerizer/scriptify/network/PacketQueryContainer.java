package mods.thecomputerizer.scriptify.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.scriptify.client.ScriptifyClient;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmd;
import mods.thecomputerizer.theimpossiblelibrary.network.MessageImpl;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketQueryContainer extends MessageImpl {

    private SubCmd sub;

    public PacketQueryContainer() {}

    public PacketQueryContainer(SubCmd sub) {
        this.sub = sub;
    }

    @Override
    public IMessage handle(MessageContext ctx) {
        ScriptifyClient.containerPos(this.sub);
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.sub = SubCmd.buildFromPacket(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        this.sub.send(buf);
    }
}

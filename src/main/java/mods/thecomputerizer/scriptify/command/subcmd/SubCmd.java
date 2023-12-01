package mods.thecomputerizer.scriptify.command.subcmd;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.command.ISubType;
import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.*;

public abstract class SubCmd extends AbstractCommand implements ISubType<AbstractCommand> {

    public static SubCmd buildFromPacket(ByteBuf buf) {
        SubCmd sub = (SubCmd)Type.getSubCmd(NetworkUtil.readString(buf)).make();
        sub.parameters = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,Parameter::read);
        return sub;
    }

    private final Type type;
    private SubCmd nextSubCmd;
    private Map<String,Parameter<?>> parameters;

    public SubCmd(Type type, Type ... subTypes) {
        super(subTypes);
        this.type = type;
        this.parameters = new HashMap<>();
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public ISubType<AbstractCommand> collect(String ... args) throws CommandException {
        if(!hasParameters()) {
            if(args.length==0) throwGeneric(array("usage"), validSubs());
            SubCmd sub = getSubCommand(args[0]);
            if(Objects.nonNull(sub)) this.nextSubCmd = (SubCmd)sub.collect(nextArgs(args));
            else throwGeneric(array("unknown"), args[0]);
        }
        for(String arg : args) {
            Parameter<?> parameter = collectParameter(arg);
            if(Objects.nonNull(parameter))
                this.parameters.put(parameter.getName(),(Parameter<?>)parameter.collect(arg));
        }
        return this;
    }

    protected Parameter<?> collectParameter(@Nullable String arg) throws CommandException {
        if(Objects.isNull(arg) || arg.isEmpty()) throwGeneric(array("unknown"),arg);
        else
            for(ISubType<?> sub : this.subTypes)
                if(sub instanceof Parameter<?> && sub.getName().matches(arg.split("=",2)[0]))
                    return (Parameter<?>)sub;
        return null;
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        if(Objects.nonNull(this.nextSubCmd)) this.nextSubCmd.execute(server,sender);
        else throwGeneric(array("execute"));
        return this;
    }

    protected abstract void executeOnPacket(MinecraftServer server, @Nullable EntityPlayerMP player, PacketSendContainerInfo packet);


    @Override
    public String getName() {
        return this.type.name;
    }

    protected Parameter<?> getParameter(String name) {
        Parameter<?> parameter = this.parameters.get(name);
        if(Objects.isNull(parameter)) {
            try {
                parameter = collectParameter(name);
            } catch (CommandException ignored) {}
        }
        if(Objects.isNull(parameter))
            ScriptifyRef.LOGGER.error("Unable to get unknown parameter `{}` for sub command `{}`!",
                    name,getName());
        return parameter;
    }


    @Override
    public List<String> getTabCompletions(String ... args) {
        if(args.length==0) return Collections.emptyList();
        if(args.length==1 || hasParameters()) return findMatchingSubTypes(args[args.length-1]);
        SubCmd sub = getSubCommand(args[0]);
        return Objects.nonNull(sub) ? sub.getTabCompletions(Arrays.copyOfRange(args,1,args.length)) :
                Collections.emptyList();
    }

    public void handlePacket(PacketSendContainerInfo packet, EntityPlayerMP player) {
        executeOnPacket(player.getServer(),player,packet);
    }

    @Override
    public void send(ByteBuf buf) {
        NetworkUtil.writeString(buf,this.getName());
        NetworkUtil.writeGenericMap(buf,this.parameters,NetworkUtil::writeString,(buf1,p) -> p.send(buf1));
    }

    @Override
    public String getLang(String ... args) {
        return Scriptify.langKey("commands",args);
    }

    @Override
    public boolean isParameter() {
        return false;
    }

    protected abstract boolean hasParameters();

    @Override
    public String toString() {
        return this.type.toString();
    }
}
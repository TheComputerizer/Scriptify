package mods.thecomputerizer.scriptify.command.subcmd;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.command.AbstractCommand;
import mods.thecomputerizer.scriptify.command.ISubType;
import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;

public abstract class SubCmd extends AbstractCommand implements ISubType<AbstractCommand> {

    public static SubCmd buildFromPacket(ByteBuf buf) {
        SubCmd sub = (SubCmd)Type.getSubCmd(NetworkUtil.readString(buf)).make();
        sub.parameters = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,Parameter::read);
        return sub;
    }


    @Getter private final Type type;
    private final boolean canSaveParameters;
    private SubCmd nextSubCmd;
    private Map<String,Parameter<?>> parameters;
    protected Collection<String> parameterSets;

    public SubCmd(Type type, Type ... subTypes) {
        super(subTypes);
        this.subTypes.add(Type.PARAMETER_SAVE_COMMAND.make());
        this.type = type;
        this.parameters = new HashMap<>();
        boolean checkSave = false;
        for(Type sub : subTypes) {
            if(sub==Type.PARAMETER_SAVE_PARAMETERS) {
                checkSave = true;
                break;
            }
        }
        this.canSaveParameters = checkSave;
    }

    public void afterExecute(MinecraftServer server, ICommandSender sender, @Nullable SubCmd parent) {
        if(Objects.isNull(parent)) parent = this;
        if(Objects.nonNull(this.nextSubCmd)) this.nextSubCmd.afterExecute(server,sender,parent);
        else {
            saveParameters(server,sender);
            saveCommand(server,sender,parent);
        }
    }

    private void appendSubCmd(StringBuilder builder, SubCmd command) {
        builder.append(command.getName());
        if(Objects.nonNull(command.nextSubCmd)) {
            builder.append(" ");
            appendSubCmd(builder,command.nextSubCmd);
        }
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
                if(sub instanceof Parameter<?> && sub.getName().matches(arg.split("=", 2)[0]))
                    return (Parameter<?>) sub;
        return null;
    }

    @SuppressWarnings("unchecked")
    protected void defineParameterSets(MinecraftServer server, ICommandSender sender) {
        if(Objects.isNull(this.parameterSets)) {
            Parameter<?> parameter = this.parameters.get("parameters");
            if(Objects.isNull(parameter)) this.parameterSets = Collections.emptyList();
            else {
                try {
                    this.parameterSets = (List<String>)parameter.execute(server,sender);
                } catch(CommandException ex) {
                    Scriptify.logError("Failed to parse parameter sets!",ex);
                }
            }
        }
    }

    @Override
    public AbstractCommand execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        if(Objects.nonNull(this.nextSubCmd)) this.nextSubCmd.execute(server,sender);
        else throwGeneric(array("execute"));
        return this;
    }

    protected abstract void executeOnPacket(MinecraftServer server, @Nullable EntityPlayerMP player, PacketSendContainerInfo packet);

    @Override
    public String getLang(String ... args) {
        return Scriptify.langKey("commands",args);
    }

    @Override
    public String getName() {
        return this.type.name;
    }

    protected Parameter<?> getParameter(Type runningType, String name) {
        Parameter<?> parameter = this.parameters.get(name);
        if(Objects.isNull(parameter)) {
            try {
                parameter = collectParameter(name);
            } catch (CommandException ex) {
                Scriptify.logError("An error occured when trying to collect a parameter!",ex);
            }
        }
        if(Objects.isNull(parameter))
            Scriptify.logError("Unable to get unknown parameter `{}` for sub command `{}`!",
                    name,getName());
        else {
            parameter.setParameterSets(this.parameterSets);
            parameter.setRunningType(runningType);
        }
        return parameter;
    }

    private List<String> getTabCompletionParameter(String ... args) {
        List<String> otherArgs = new ArrayList<>();
        if(args.length>1)
            for(String arg : Arrays.copyOfRange(args,0,args.length-1))
                otherArgs.add(arg.split("=",2)[0]);
        String arg = args[args.length-1];
        String[] split = arg.split("=",2);
        String name = split[0];
        String val = split.length>1 ? split[1] : "";
        List<Parameter<?>> parameterList = new ArrayList<>();
        for(ISubType<?> sub : this.subTypes)
            if(!otherArgs.contains(sub.getName()) && sub.getName().startsWith(name))
                parameterList.add((Parameter<?>)sub);
        for(Parameter<?> parameter : parameterList) parameter.setRunningType(this.type);
        if(parameterList.size()==1) return parameterList.get(0).getTabCompletions(val);
        List<String> options = new ArrayList<>();
        for(Parameter<?> parameter : parameterList) {
            String pName = parameter.getName()+"=";
            if(pName.startsWith(name)) options.add(pName);
        }
        return options;
    }

    @Override
    public List<String> getTabCompletions(String ... args) {
        if(this.hasParameters())
            return args.length==0 ? Collections.emptyList() : getTabCompletionParameter(args);
        else {
            if(args.length>=1) {
                SubCmd sub = getSubCommand(args[0]);
                return Objects.nonNull(sub) ? sub.getTabCompletions(nextArgs(args)) : Collections.emptyList();
            } else return Collections.emptyList();
        }
    }

    public void handlePacket(PacketSendContainerInfo packet, EntityPlayerMP player) {
        executeOnPacket(player.getServer(),player,packet);
    }

    protected abstract boolean hasParameters();

    @Override
    public boolean isParameter() {
        return false;
    }

    @Override
    protected String lang(String ... args) {
        return getLang(args);
    }

    protected void saveParameters(MinecraftServer server, ICommandSender sender) {
        if(this.canSaveParameters) {
            try {
                String saveParameters = (String) getParameter(this.getType(),"saveParameters").execute(server,sender);
                if(saveParameters.matches("null") || StringUtils.isBlank(saveParameters)) return;
                String[] parameterDefaults = new String[this.parameters.size()];
                int index = 0;
                for(Parameter<?> parameter : this.parameters.values()) {
                    parameter.saveCollectedValue(parameterDefaults, index);
                    index++;
                }
                ScriptifyConfigHelper.addToCache("parameters",saveParameters,parameterDefaults);
            } catch(CommandException ex) {
                Scriptify.logError("Unable to save parameters values!",ex);
            }
        }
    }

    protected void saveCommand(MinecraftServer server, ICommandSender sender, SubCmd parent) {
        try {
            String saveCommand = (String) getParameter(this.getType(), "saveCommand").execute(server, sender);
            if(saveCommand.matches("null") || StringUtils.isBlank(saveCommand)) return;
            StringBuilder builder = new StringBuilder(ScriptifyRef.MODID+" ");
            appendSubCmd(builder,parent);
            String[] parameterArray = new String[this.parameters.size()];
            int index = 0;
            for(Parameter<?> parameter : this.parameters.values()) {
                parameter.saveCollectedValue(parameterArray,index);
                index++;
            }
            for(String parameter : parameterArray) builder.append(" ").append(parameter);
            ScriptifyConfigHelper.addToCache("commands",saveCommand,builder.toString());
        } catch(CommandException ex) {
            Scriptify.logError("Unable to save running command!",ex);
        }
    }

    @Override
    public void send(ByteBuf buf) {
        NetworkUtil.writeString(buf,this.getName());
        NetworkUtil.writeGenericMap(buf,this.parameters,NetworkUtil::writeString,(buf1,p) -> p.send(buf1));
    }

    @Override
    protected void sendSuccess(ICommandSender sender, Object ... parameters) {
        sendGeneric(sender,array(getName(),"success"),parameters);
    }

    @Override
    public String toString() {
        return this.type.toString();
    }
}
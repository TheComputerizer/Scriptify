package mods.thecomputerizer.scriptify.command.parameters;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.command.ISubType;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public abstract class Parameter<T> implements ISubType<T> {

    protected Collection<String> parameterSets = new ArrayList<>();
    protected Type runningType;

    public static Parameter<?> read(ByteBuf buf) {
        Parameter<?> parameter = (Parameter<?>)Type.getParameter(NetworkUtil.readString(buf)).make();
        parameter.valueStr = NetworkUtil.readString(buf);
        return parameter;
    }

    @Getter private final Type type;
    @Setter protected String valueStr;

    public Parameter(Type type) {
        this.type = type;
        this.runningType = type;
    }

    @Override
    public ISubType<T> collect(String ... args) throws CommandException {
        this.valueStr = getValue(args[0],true);
        return this;
    }

    @Override
    public T execute(MinecraftServer server, ICommandSender sender) throws CommandException {
        if(Objects.isNull(this.valueStr)) this.valueStr = ScriptifyConfigHelper.getDefaultParameter(this.parameterSets,this.getName());
        return parse(server,sender,this.valueStr);
    }

    @Override
    public String getLang(String ... args) {
        return Scriptify.langKey("parameters",args);
    }

    @Override
    public String getName() {
        return this.type.name;
    }

    public String getValue(String unparsed, boolean withSpacing) throws CommandException {
        String[] split = unparsed.split("=",2);
        if(split.length<2)
            throw new CommandException(inject("Unable to parse empty value for parameter `{}`",split[0]));
        return withSpacing ? withSpacing(split[1]) : split[1];
    }


    @SuppressWarnings("SameParameterValue")
    private String inject(String str, Object ... args) {
        for(Object arg : args) str = str.replaceFirst("\\{}",arg.toString());
        return str;
    }

    @Override
    public boolean isParameter() {
        return true;
    }

    protected abstract T parse(MinecraftServer server, ICommandSender sender, String valueStr) throws CommandException;

    public void saveCollectedValue(String[] array, int index) {
        array[index] = this.getName()+"="+this.valueStr;
    }

    @Override
    public void send(ByteBuf buf) {
        NetworkUtil.writeString(buf,this.getName());
        if(Objects.isNull(this.valueStr) || this.valueStr.isEmpty()) this.valueStr = getType().getDefVal();
        NetworkUtil.writeString(buf,this.valueStr);
    }

    public void setParameterSets(Collection<String> parameterSets) {
        this.parameterSets = parameterSets;
    }

    public void setRunningType(Type runningType) {
        this.runningType = runningType;
    }

    public void throwGeneric(String type, Object ... args) throws CommandException {
        throw new CommandException(Scriptify.langKey("parameter",type),args);
    }

    @Override
    public String toString() {
        return this.type.toString();
    }

    public String withSpacing(String raw) {
        return raw.replaceAll("\\^_"," ").trim();
    }
}

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
import java.util.List;
import java.util.Objects;

public abstract class Parameter<T> implements ISubType<T> {

    public static Parameter<?> read(ByteBuf buf) {
        Parameter<?> parameter = (Parameter<?>)Type.getParameter(NetworkUtil.readString(buf)).make();
        parameter.valueStr = NetworkUtil.readString(buf);
        return parameter;
    }

    @Getter private final Type type;
    @Setter protected Collection<String> parameterSets = new ArrayList<>();
    @Setter protected Type runningType;
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
    public String getLang(String ... args) {
        return Scriptify.makeLangKey("parameters",args);
    }

    @Override
    public String getName() {
        return this.type.name;
    }

    public abstract byte getAsByte() throws CommandException;

    public abstract boolean getAsBool() throws CommandException;

    public abstract double getAsDouble() throws CommandException;

    public abstract float getAsFloat() throws CommandException;

    public abstract int getAsInt() throws CommandException;

    public abstract long getAsLong() throws CommandException;

    public abstract Number getAsNumber() throws CommandException;

    public abstract short getAsShort() throws CommandException;

    public abstract String getAsString() throws CommandException;

    public abstract <E> List<E> getAsList() throws CommandException;

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

    public T parse() throws CommandException {
        if(Objects.isNull(this.valueStr)) this.valueStr = ScriptifyConfigHelper.getDefaultParameter(this.parameterSets,this.getName());
        return parse(this.valueStr);
    }

    protected abstract T parse(String valueStr) throws CommandException;

    public void saveCollectedValue(String[] array, int index) {
        array[index] = this.getName()+"="+this.valueStr;
    }

    @Override
    public void send(ByteBuf buf) {
        NetworkUtil.writeString(buf,this.getName());
        if(Objects.isNull(this.valueStr) || this.valueStr.isEmpty()) this.valueStr = getType().getDefVal();
        NetworkUtil.writeString(buf,this.valueStr);
    }

    public void throwGeneric(String type, Object ... args) throws CommandException {
        throw new CommandException(Scriptify.makeLangKey("parameter",type),args);
    }

    @Override
    public String toString() {
        try {
            return getAsString();
        } catch(CommandException ex) {
            return this.type.toString();
        }
    }

    public String withSpacing(String raw) {
        return raw.replaceAll("\\^_"," ").trim();
    }
}

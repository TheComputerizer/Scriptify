package mods.thecomputerizer.scriptify.command.parameters;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.command.ISubType;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Parameter<T> implements ISubType<T> {

    private final Type type;
    protected String valueStr;

    public Parameter(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public ISubType<T> collect(String ... args) throws CommandException {
        this.valueStr = getValue(args[0],true);
        return this;
    }

    @Override
    public T execute(MinecraftServer server, ICommandSender sender) throws CommandException {
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

    @Override
    public boolean isParameter() {
        return true;
    }

    @Override
    public String toString() {
        return this.type.toString();
    }


    @SuppressWarnings("SameParameterValue")
    private String inject(String str, Object ... args) {
        for(Object arg : args) str = str.replaceFirst("\\{}",arg.toString());
        return str;
    }

    public String getValue(String unparsed, boolean withSpacing) throws CommandException {
        String[] split = unparsed.split("=",2);
        if(split.length<2)
            throw new CommandException(inject("Unable to parse empty value for parameter `{}`",split[0]));
        return withSpacing ? withSpacing(split[1]) : split[1];
    }

    public void throwGeneric(String type, Object ... args) throws CommandException {
        throw new CommandException(Scriptify.langKey("parameter",type),args);
    }

    public String withSpacing(String raw) {
        return raw.replaceAll("\\^_"," ").trim();
    }

    protected abstract T parse(MinecraftServer server, ICommandSender sender, String valueStr) throws CommandException;

    public enum Type {

        NAME("name", ParameterName::new),
        TOTAL_SLOTS("totalSlots", ParameterTotalSlots::new);

        private static final Map<String,Type> BY_NAME = new HashMap<>();

        public static Type get(String name) {
            return BY_NAME.getOrDefault(name,TOTAL_SLOTS);
        }

        public final String name;
        private final Supplier<Parameter<?>> parameterSupplier;

        Type(String name, Supplier<Parameter<?>> parameterSupplier) {
            this.name = name;
            this.parameterSupplier = parameterSupplier;
        }

        @Override
        public String toString() {
            return this.name;
        }

        public Parameter<?> make() {
            return this.parameterSupplier.get();
        }

        static {
            for(Type type : values()) BY_NAME.put(type.name,type);
        }
    }
}

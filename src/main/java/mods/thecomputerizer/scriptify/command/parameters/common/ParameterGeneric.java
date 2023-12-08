package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import net.minecraft.command.CommandException;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ParameterGeneric<T> extends Parameter<T> {

    private final Function<String,T> parser;

    public ParameterGeneric(Type type, Function<String,T> parser) {
        super(type);
        this.parser = parser;
    }

    @Override
    public byte getAsByte() throws CommandException {
        return getAsNumber().byteValue();
    }

    @Override
    public boolean getAsBool() throws CommandException {
        return Boolean.parseBoolean(getAsString());
    }

    @Override
    public double getAsDouble() throws CommandException {
        return getAsNumber().doubleValue();
    }

    @Override
    public float getAsFloat() throws CommandException {
        return getAsNumber().floatValue();
    }

    @Override
    public int getAsInt() throws CommandException {
        return getAsNumber().intValue();
    }

    @Override
    public long getAsLong() throws CommandException {
        return getAsNumber().longValue();
    }

    @Override
    public Number getAsNumber() throws CommandException {
        return Double.parseDouble(getAsString());
    }

    @Override
    public short getAsShort() throws CommandException {
        return getAsNumber().shortValue();
    }

    @Override
    public String getAsString() throws CommandException {
        return parse().toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> getAsList() throws CommandException {
        return (List<E>)Collections.singletonList(parse());
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        return Collections.emptyList();
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    protected T parse(String valueStr) {
        return this.parser.apply(valueStr);
    }
}

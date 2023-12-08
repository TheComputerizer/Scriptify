package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import net.minecraft.command.CommandException;

import java.util.Collections;
import java.util.List;

public class ParameterString extends Parameter<String> {

    public ParameterString(Type type) {
        super(type);
    }

    @Override
    public byte getAsByte() throws CommandException {
        return Byte.parseByte(parse());
    }

    @Override
    public boolean getAsBool() throws CommandException{
        return Boolean.parseBoolean(parse());
    }

    @Override
    public double getAsDouble() throws CommandException {
        return Double.parseDouble(parse());
    }

    @Override
    public float getAsFloat() throws CommandException {
        return Float.parseFloat(parse());
    }

    @Override
    public int getAsInt() throws CommandException {
        return Integer.parseInt(parse());
    }

    @Override
    public long getAsLong() throws CommandException {
        return Long.parseLong(parse());
    }

    @Override
    public Number getAsNumber() throws CommandException {
        return getAsDouble();
    }

    @Override
    public short getAsShort() throws CommandException {
        return Short.parseShort(parse());
    }

    @Override
    public String getAsString() throws CommandException {
        return parse();
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
    protected String parse(String valueStr) {
        return valueStr;
    }
}

package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import mods.thecomputerizer.scriptify.command.parameters.Parser;
import mods.thecomputerizer.scriptify.util.TabCompletions;
import net.minecraft.command.CommandException;

import java.util.Collections;
import java.util.List;

public class ParameterBoolean extends Parameter<Boolean> {

    public ParameterBoolean(Type type) {
        super(type);
    }

    @Override
    public byte getAsByte() throws CommandException {
        return getAsNumber().byteValue();
    }

    @Override
    public boolean getAsBool() throws CommandException {
        return parse();
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
        return parse() ? 1 : 0;
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
    public List<String> getTabCompletions(String ... args) {
        return TabCompletions.getStrictOptions(getName()+"=",args[0],"false","true");
    }

    @Override
    public int isRequired() {
        return 0;
    }

    @Override
    protected Boolean parse(String valueStr) {
        return Parser.parseBool(valueStr);
    }
}

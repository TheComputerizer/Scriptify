package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import net.minecraft.command.CommandException;

import java.util.Collections;
import java.util.List;

/**
 * This class might be unnecessary
 */
public abstract class ParameterNumber<N extends Number> extends Parameter<N> {

    protected ParameterNumber(Type type) {
        super(type);
    }

    @Override
    public byte getAsByte() throws CommandException {
        return getAsNumber().byteValue();
    }

    @Override
    public boolean getAsBool() {
        return false;
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
        return parse();
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
}

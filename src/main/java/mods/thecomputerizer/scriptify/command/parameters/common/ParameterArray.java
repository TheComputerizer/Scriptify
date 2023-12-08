package mods.thecomputerizer.scriptify.command.parameters.common;

import mods.thecomputerizer.scriptify.command.parameters.Parameter;
import mods.thecomputerizer.scriptify.command.parameters.Parser;
import net.minecraft.command.CommandException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class ParameterArray<T,P extends Parameter<T>> extends Parameter<List<T>> {

    private final P elementReference;

    protected ParameterArray(Type type, Function<Type,P> referenceCreator) {
        super(type);
        this.elementReference = referenceCreator.apply(type);
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
        List<P> list = getAsList();
        return list.isEmpty() ? 0 : list.get(0).getAsNumber();
    }

    @Override
    public short getAsShort() throws CommandException {
        return getAsNumber().shortValue();
    }

    @Override
    public String getAsString() throws CommandException {
        List<T> list = parse();
        if(Objects.isNull(list) || list.isEmpty()) return "";
        Object element = list.get(0);
        return Objects.nonNull(element) ? element.toString() : "";
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> getAsList() throws CommandException {
        return (List<E>)parse();
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
    protected List<T> parse(String valueStr) throws CommandException {
        return Parser.parseArray(this.elementReference,valueStr);
    }
}

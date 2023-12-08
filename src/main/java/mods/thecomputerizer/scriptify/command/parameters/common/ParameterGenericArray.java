package mods.thecomputerizer.scriptify.command.parameters.common;

import java.util.function.Function;

public class ParameterGenericArray<T> extends ParameterArray<T,ParameterGeneric<T>> {

    public ParameterGenericArray(Type type, Function<String,T> parser) {
        super(type,type1 -> new ParameterGeneric<>(type1,parser));
    }
}

package mods.thecomputerizer.scriptify.command.parameters.common;

import java.util.function.Function;

/**
 * This class might be unnecessary
 */
public abstract class ParameterNumberArray<N extends Number,P extends ParameterNumber<N>> extends ParameterArray<N,P> {

    protected ParameterNumberArray(Type type, Function<Type, P> referenceCreator) {
        super(type,referenceCreator);
    }
}

package mods.thecomputerizer.scriptify.io.data;

import lombok.Getter;
import mods.thecomputerizer.scriptify.util.Misc;
import stanhebben.zenscript.compiler.IEnvironmentMethod;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.type.ZenTypeAssociative;

/**
 * Holds cast expressions so they can be written if the need arises
 */
public class ExpressionCastHolder extends Expression {

    @Getter private final Expression expression;
    private final ZenType type;
    @Getter private final String importName;
    @Getter private final String qualifier;

    public ExpressionCastHolder(Expression expression, ZenType type) {
        super(expression.getPosition());
        this.expression = expression;
        this.type = type instanceof ZenTypeAssociative ? ((ZenTypeAssociative)type).getValueType() : type;
        String name = this.type.getName();
        this.importName = name.contains(".") ? name : "";
        this.qualifier = " as "+ Misc.getLastSplit(name,".");
    }

    @Override
    public void compile(boolean result, IEnvironmentMethod environment) {

    }

    @Override
    public ZenType getType() {
        return this.type;
    }
}

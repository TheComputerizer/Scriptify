package mods.thecomputerizer.scriptify.io.data;

import lombok.Getter;
import stanhebben.zenscript.compiler.IEnvironmentMethod;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.type.ZenType;

/**
 * Holds call expressions so they don't try to get parsed automatically
 */
@Getter
public class ExpressionCallHolder extends Expression {

    private final Expression receiver;
    private final String methodName;
    private final Expression[] arguments;

    public ExpressionCallHolder(Expression receiver, String methodName, Expression ... args) {
        super(receiver.getPosition());
        this.receiver = receiver;
        this.methodName = methodName;
        this.arguments = args;
    }

    @Override
    public void compile(boolean result, IEnvironmentMethod environment) {

    }

    @Override
    public ZenType getType() {
        return this.receiver.getType();
    }
}

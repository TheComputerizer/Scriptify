package mods.thecomputerizer.scriptify.mixin.mods;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.parser.expression.ParsedExpressionBinary;

@Mixin(value = ParsedExpressionBinary.class, remap = false)
public interface ParsedExpressionBinaryAccessor {

    @Accessor ParsedExpression getLeft();
    @Accessor ParsedExpression getRight();
    @Accessor OperatorType getOperator();
}

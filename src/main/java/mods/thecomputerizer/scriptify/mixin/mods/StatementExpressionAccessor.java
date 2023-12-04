package mods.thecomputerizer.scriptify.mixin.mods;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.statements.StatementExpression;

@Mixin(value = StatementExpression.class, remap = false)
public interface StatementExpressionAccessor {

    @Accessor ParsedExpression getExpression();
}

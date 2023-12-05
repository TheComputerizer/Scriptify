package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.parser.expression.ParsedExpressionCall;

import java.util.List;

@Mixin(value = ParsedExpressionCall.class, remap = false)
public interface ParsedExpressionCallAccessor {

    @Accessor ParsedExpression getReceiver();
    @Accessor List<ParsedExpression> getArguments();
}

package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.parser.expression.ParsedExpressionCast;
import stanhebben.zenscript.type.ZenType;

@Mixin(value = ParsedExpressionCast.class, remap = false)
public interface ParsedExpressionCastAccessor {

    @Accessor ParsedExpression getValue();
    @Accessor ZenType getType();
}

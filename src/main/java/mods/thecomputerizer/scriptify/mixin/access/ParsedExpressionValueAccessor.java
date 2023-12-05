package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.expression.partial.IPartialExpression;
import stanhebben.zenscript.parser.expression.ParsedExpressionValue;

@Mixin(value = ParsedExpressionValue.class, remap = false)
public interface ParsedExpressionValueAccessor {

    @Accessor IPartialExpression getValue();
}

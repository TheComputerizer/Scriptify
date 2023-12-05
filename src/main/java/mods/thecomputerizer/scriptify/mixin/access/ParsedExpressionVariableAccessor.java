package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.parser.expression.ParsedExpressionVariable;

@Mixin(value = ParsedExpressionVariable.class, remap = false)
public interface ParsedExpressionVariableAccessor {

    @Accessor String getName();
}

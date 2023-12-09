package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionArray;

@Mixin(value = ExpressionArray.class, remap = false)
public interface ExpressionArrayAccessor {

    @Accessor Expression[] getContents();
}

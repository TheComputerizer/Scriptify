package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionMap;

@Mixin(value = ExpressionMap.class, remap = false)
public interface ExpressionMapAccessor {

    @Accessor Expression[] getKeys();
    @Accessor Expression[] getValues();
}

package mods.thecomputerizer.scriptify.mixin.mods;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.expression.ExpressionInt;

@Mixin(value = ExpressionInt.class, remap = false)
public interface ExpressionIntAccessor {

    @Accessor long getValue();
}

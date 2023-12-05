package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.expression.ExpressionFloat;

@Mixin(value = ExpressionFloat.class, remap = false)
public interface ExpressionFloatAccessor {

    @Accessor double getValue();
}

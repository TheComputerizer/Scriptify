package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.expression.ExpressionBool;

@Mixin(value = ExpressionBool.class, remap = false)
public interface ExpressionBoolAccessor {

    @Accessor boolean getValue();
}

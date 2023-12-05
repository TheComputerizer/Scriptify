package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.expression.ExpressionString;

@Mixin(value = ExpressionString.class, remap = false)
public interface ExpressionStringAccessor {

    @Accessor String getValue();
}

package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionAs;
import stanhebben.zenscript.type.casting.ICastingRule;

@Mixin(value = ExpressionAs.class, remap = false)
public interface ExpressionAsAccessor {

    @Accessor Expression getValue();
    @Accessor ICastingRule getCastingRule();
}

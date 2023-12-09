package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionCallVirtual;
import stanhebben.zenscript.type.natives.IJavaMethod;

@Mixin(value = ExpressionCallVirtual.class, remap = false)
public interface ExpressionCallVirtualAccessor {

    @Accessor IJavaMethod getMethod();
    @Accessor Expression getReceiver();
    @Accessor Expression[] getArguments();
}

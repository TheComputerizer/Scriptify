package mods.thecomputerizer.scriptify.mixin.mods;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.type.natives.IJavaMethod;
import stanhebben.zenscript.util.Pair;

import java.util.List;

@Mixin(value = ExpressionCallStatic.class, remap = false)
public interface ExpressionCallStaticAccessor {

    @Accessor IJavaMethod getMethod();
    @Accessor Expression[] getArguments();
    @Accessor List<Pair<ZenType,ParsedExpression>> getFilledDefaultValues();
}

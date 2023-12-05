package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.parser.expression.ParsedExpressionArray;

import java.util.List;

@Mixin(value = ParsedExpressionArray.class, remap = false)
public interface ParsedExpressionArrayAccessor {

    @Accessor List<ParsedExpression> getContents();
}

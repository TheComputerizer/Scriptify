package mods.thecomputerizer.scriptify.mixin.mods;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.parser.expression.ParsedExpressionMap;

import java.util.List;

@Mixin(value = ParsedExpressionMap.class, remap = false)
public interface ParsedExpressionMapAccessor {

    @Accessor List<ParsedExpression> getKeys();
    @Accessor List<ParsedExpression> getValues();
}

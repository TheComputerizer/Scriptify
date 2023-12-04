package mods.thecomputerizer.scriptify.mixin.mods;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.parser.expression.ParsedExpressionMember;

@Mixin(value = ParsedExpressionMember.class, remap = false)
public interface ParsedExpressionMemberAccessor {

    @Accessor ParsedExpression getValue();
    @Accessor String getMember();
}

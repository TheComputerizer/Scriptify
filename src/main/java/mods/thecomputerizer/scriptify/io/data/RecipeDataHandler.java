package mods.thecomputerizer.scriptify.io.data;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.mixin.access.ParsedExpressionCallAccessor;
import mods.thecomputerizer.scriptify.mixin.access.ParsedExpressionMemberAccessor;
import mods.thecomputerizer.scriptify.mixin.access.ParsedExpressionVariableAccessor;
import mods.thecomputerizer.scriptify.mixin.access.StatementExpressionAccessor;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.parser.expression.ParsedExpressionCall;
import stanhebben.zenscript.parser.expression.ParsedExpressionMember;
import stanhebben.zenscript.statements.StatementExpression;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("SpellCheckingInspection")
public class RecipeDataHandler {

    private static final List<RecipeBlueprint> BLUEPRINT_LIST = new ArrayList<>();
    public static final RecipeBlueprint CRAFTING_SHAPED_BLUEPRINT = addBluePrint(new RecipeBlueprint("recipes",
            "addShaped","string","item","ingredient[][]"));
    public static final RecipeBlueprint CRAFTING_SHAPELESS_BLUEPRINT = addBluePrint(new RecipeBlueprint("recipes",
            "addShapeless","string","item","ingredient[]"));
    public static final RecipeBlueprint EXTENDED_SHAPED_BLUEPRINT = addBluePrint(new RecipeBlueprint(
            "mods.extendedcrafting.TableCrafting", "addShaped","int","item","ingredient[][]"));
    public static final RecipeBlueprint EXTENDED_SHAPELESS_BLUEPRINT = addBluePrint(new RecipeBlueprint(
            "mods.extendedcrafting.TableCrafting", "addShapeless","int","item","ingredient[]"));
    public static final RecipeBlueprint FURNACE_BLUEPRINT = addBluePrint(new RecipeBlueprint("furnace",
            "addRecipe","item","item","float"));

    public static RecipeBlueprint addBluePrint(RecipeBlueprint blueprint) {
        BLUEPRINT_LIST.add(blueprint);
        return blueprint;
    }

    public static @Nullable ParsedRecipeData matchExpression(StatementExpression statement) throws IllegalArgumentException {
        ParsedExpression expression = ((StatementExpressionAccessor)statement).getExpression();
        if(expression instanceof ParsedExpressionCall) {
            ParsedExpressionCallAccessor access = (ParsedExpressionCallAccessor)expression;
            RecipeBlueprint blueprint = matchMember((ParsedExpressionMember)access.getReceiver());
            if(Objects.nonNull(blueprint)) {
                Scriptify.logInfo("Successfully located blueprint {}",blueprint);
                return new ParsedRecipeData(blueprint,access.getArguments());
            }
        } else Scriptify.logError("Expression of class {} cannot be parsed into recipe data!",expression.getClass().getName());
        return null;
    }

    public static @Nullable RecipeBlueprint matchMember(ParsedExpressionMember member) {
        ParsedExpressionMemberAccessor access = (ParsedExpressionMemberAccessor)member;
        String methodName = access.getMember();
        String className = ((ParsedExpressionVariableAccessor)access.getValue()).getName();
        return getBlueprint(className,methodName);
    }

    public static @Nullable RecipeBlueprint getBlueprint(String className, String methodName) {
        for(RecipeBlueprint blueprint : BLUEPRINT_LIST)
            if(blueprint.matches(className,methodName)) return blueprint;
        Scriptify.logError("Unable to find matching recipe type for {}#{}!",className,methodName);
        return null;
    }
}

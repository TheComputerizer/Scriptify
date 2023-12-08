package mods.thecomputerizer.scriptify.io.data;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.mixin.access.ParsedExpressionCallAccessor;
import mods.thecomputerizer.scriptify.mixin.access.ParsedExpressionMemberAccessor;
import mods.thecomputerizer.scriptify.mixin.access.ParsedExpressionVariableAccessor;
import mods.thecomputerizer.scriptify.mixin.access.StatementExpressionAccessor;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.parser.expression.ParsedExpressionCall;
import stanhebben.zenscript.parser.expression.ParsedExpressionMember;
import stanhebben.zenscript.statements.StatementExpression;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("SpellCheckingInspection")
public class RecipeDataHandler {

    private static final List<RecipeBlueprint> BLUEPRINT_LIST = new ArrayList<>();
    public static final RecipeBlueprint CRAFTING_SHAPED_BLUEPRINT = addBluePrint(new RecipeBlueprint("vanilla",
            "recipes","addShaped","string","item","ingredient[][]"));
    public static final RecipeBlueprint CRAFTING_SHAPELESS_BLUEPRINT = addBluePrint(new RecipeBlueprint("vanilla",
            "recipes","addShapeless","string","item","ingredient[]"));
    public static final RecipeBlueprint EXTENDED_SHAPED_BLUEPRINT = addBluePrint(new RecipeBlueprint(
            "extendedcrafting", "mods.extendedcrafting.TableCrafting", "addShaped",
            "int","item", "ingredient[][]"));
    public static final RecipeBlueprint EXTENDED_SHAPELESS_BLUEPRINT = addBluePrint(new RecipeBlueprint(
            "extendedcrafting", "mods.extendedcrafting.TableCrafting", "addShapeless",
            "int","item","ingredient[]"));
    public static final RecipeBlueprint FURNACE_BLUEPRINT = addBluePrint(new RecipeBlueprint("vanilla",
            "furnace", "addRecipe","item","item","float"));

    public static RecipeBlueprint addBluePrint(RecipeBlueprint blueprint) {
        BLUEPRINT_LIST.add(blueprint);
        return blueprint;
    }

    public static @Nullable ParsedRecipeData matchFilteredExpression(StatementExpression statement,
            Collection<String> classMatches, Collection<String> methodMatches, boolean isDebug) throws IllegalArgumentException {
        ScriptifyRef.LOGGER.error("CLASSES ARE {} AND METHODS ARE {}",TextUtil.compileCollection(classMatches),
                TextUtil.compileCollection(methodMatches));
        ParsedExpression expression = ((StatementExpressionAccessor)statement).getExpression();
        if(expression instanceof ParsedExpressionCall) {
            ParsedExpressionCallAccessor access = (ParsedExpressionCallAccessor)expression;
            RecipeBlueprint blueprint = matchMember((ParsedExpressionMember)access.getReceiver(),classMatches,methodMatches);
            if(Objects.nonNull(blueprint)) {
                Scriptify.logInfo(RecipeDataHandler.class,null,blueprint);
                return new ParsedRecipeData(blueprint,access.getArguments());
            } else if(!classMatches.isEmpty() || !methodMatches.isEmpty()) {
                if(classMatches.isEmpty())
                    Scriptify.logError(RecipeDataHandler.class,"method",null,TextUtil.compileCollection(methodMatches));
                else Scriptify.logError(RecipeDataHandler.class,"class",null,TextUtil.compileCollection(classMatches));
            }
        } else Scriptify.logError(RecipeDataHandler.class,"match",null,expression.getClass().getName());
        return null;
    }

    public static @Nullable RecipeBlueprint matchMember(ParsedExpressionMember member, Collection<String> classMatches,
                                                        Collection<String> methodMatches) {
        ParsedExpressionMemberAccessor access = (ParsedExpressionMemberAccessor)member;
        String methodName = access.getMember();
        if(!methodMatches.isEmpty() && !methodMatches.contains(methodName)) return null;
        String className = ((ParsedExpressionVariableAccessor)access.getValue()).getName();
        if(!classMatches.isEmpty() && !classMatches.contains(className)) return null;
        return getBlueprint(className,methodName);
    }

    public static @Nullable RecipeBlueprint getBlueprint(String className, String methodName) {
        for(RecipeBlueprint blueprint : BLUEPRINT_LIST)
            if(blueprint.matches(className,methodName)) return blueprint;
        Scriptify.logError(RecipeDataHandler.class,"get",null,className,methodName);
        return null;
    }
}

package mods.thecomputerizer.scriptify.data;

import stanhebben.zenscript.ZenTokener;

@SuppressWarnings("SpellCheckingInspection")
public class RecipeDataHandler {

    public static final RecipeBlueprint CRAFTING_SHAPED_BLUEPRINT = new RecipeBlueprint("recipes",
            "addShaped","string","item","item[][]");
    public static final RecipeBlueprint CRAFTING_SHAPELESS_BLUEPRINT = new RecipeBlueprint("recipes",
            "addShapeless","string","item","item[]");
    public static final RecipeBlueprint EXTENDED_SHAPED_BLUEPRINT = new RecipeBlueprint(
            "mods.extendedcrafting.TableCrafting", "addShaped","int","item","item[][]");
    public static final RecipeBlueprint EXTENDED_SHAPELESS_BLUEPRINT = new RecipeBlueprint(
            "mods.extendedcrafting.TableCrafting", "addShapeless","int","item","item[]");
    public static final RecipeBlueprint FURNACE_BLUEPRINT = new RecipeBlueprint("furnace",
            "addRecipe","item","item","float");

}

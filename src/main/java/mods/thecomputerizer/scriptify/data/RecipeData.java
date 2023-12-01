package mods.thecomputerizer.scriptify.data;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import mods.thecomputerizer.scriptify.write.IngredientWriter;
import mods.thecomputerizer.scriptify.write.RecipeWriter;
import net.minecraft.item.ItemStack;

import java.util.Map;

public abstract class RecipeData {

    protected ItemStack[] extras;
    protected ItemStack[] inputs;
    protected Map<ItemStack,Integer> itemNumbers = new Object2IntLinkedOpenHashMap<>();

    protected String name;
    protected int[] numbers;
    protected ItemStack output;

    public RecipeData addExtra(ItemStack ... extras) {
        this.extras = extras;
        return this;
    }

    public RecipeData addInputs(ItemStack ... inputs) {
        this.inputs = inputs;
        return this;
    }

    public RecipeData addItemNumbers(Object2IntLinkedOpenHashMap<ItemStack> itemNumbers) {
        this.itemNumbers = itemNumbers;
        return this;
    }

    public RecipeData addNumbers(int ... numbers) {
        this.numbers = numbers;
        return this;
    }

    public RecipeData setName(String name) {
        this.name = name;
        return this;
    }

    public RecipeData setOutput(ItemStack output) {
        this.output = output;
        return this;
    }

    protected String writeExtras() {
        return "";
    }
    protected abstract String writeInputs();

    protected String writeName() {
        return "\""+this.name+"\"";
    }

    protected String writeNumbers() {
        return "";
    };

    protected String writeOutput() {
        return new IngredientWriter(this.output).toString();
    }

    public RecipeWriter writeRecipe() {
        return new RecipeWriter(this);
    }
}

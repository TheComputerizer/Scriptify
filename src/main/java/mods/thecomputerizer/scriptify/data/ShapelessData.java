package mods.thecomputerizer.scriptify.data;

import mods.thecomputerizer.scriptify.write.ArrayWriter;
import mods.thecomputerizer.scriptify.write.IngredientWriter;

public class ShapelessData extends RecipeData {

    @Override
    public String toString() {
        return "recipes.addShapeless("+this.writeName()+","+this.writeOutput()+","+this.writeInputs()+")";
    }

    @Override
    protected String writeInputs() {
        IngredientWriter[] writers = new IngredientWriter[this.inputs.length];
        for(int i=0; i<writers.length; i++) writers[i] = new IngredientWriter(this.inputs[i]);
        return new ArrayWriter<>(writers).toString();
    }
}

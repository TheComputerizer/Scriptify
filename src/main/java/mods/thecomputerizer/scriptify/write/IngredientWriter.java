package mods.thecomputerizer.scriptify.write;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class IngredientWriter extends UncommentedWriter<IngredientWriter> {

    private final boolean isOreDict;
    private final FluidStack fluid;
    private final ItemStack item;

    public IngredientWriter(ItemStack item) {
        this(item,false);
    }

    public IngredientWriter(ItemStack item, boolean isOreDict) {
        this.item = item;
        this.isOreDict = isOreDict;
        this.fluid = null;
    }

    public IngredientWriter(FluidStack fluid) {
        this.fluid = fluid;
        this.item = ItemStack.EMPTY;
        this.isOreDict = false;
    }

    @Override
    public void add(IStringify<?> other) {

    }

    @Override
    public List<String> stringify() {
        return Collections.singletonList(this.toString());
    }

    @Override
    public String toString() {
        return writeItem();
    }

    @Override
    public void write(String filePath, boolean overwrite) {}

    public String writeItem() {
        int meta = this.item.getMetadata(), amount = this.item.getCount();
        if(this.item.isEmpty() || amount==0 || Objects.isNull(this.item.getItem().getRegistryName())) return "null";
        else {
            String resource = this.item.getItem().getRegistryName().toString();
            return "<"+resource+(meta==0 ? "" : ":"+meta)+">"+(amount==1 ? "" : "*" + amount);
        }
    }
}

package mods.thecomputerizer.scriptify.io.data;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Objects;

public class BEP {

    private final String[] elements;
    @Getter @Setter private String extra;
    @Getter @Setter private int amount;

    public BEP(String ... elements) {
        this(1,elements);
    }

    public BEP(int amount, String ... elements) {
        this.elements = elements;
        this.amount = amount;
    }

    public FluidStack asFluid() {
        if(this.amount==0 || Objects.isNull(this.elements) || this.elements.length<2) return null;
        return FluidRegistry.getFluidStack(this.elements[1],this.amount);
    }

    public ItemStack asItem() {
        if(this.amount==0 || Objects.isNull(this.elements) || this.elements.length<2) return ItemStack.EMPTY;
        int meta = Objects.nonNull(this.extra) ? Integer.parseInt(this.extra) : 0;
        ResourceLocation resource = new ResourceLocation(this.elements[0],this.elements[1]);
        Item item = ForgeRegistries.ITEMS.containsKey(resource) ? ForgeRegistries.ITEMS.getValue(resource) : null;
        return Objects.nonNull(item) ? new ItemStack(item,this.amount,meta) : ItemStack.EMPTY;
    }

    public NonNullList<ItemStack> asOreEntries() {
        if(this.amount==0 || Objects.isNull(this.elements) || this.elements.length<2) return null;
        return OreDictionary.getOres(this.elements[1]);
    }

    @Override
    public String toString() {
        String ret = "null";
        if(this.amount>0) {
            ret = TextUtil.arrayToString(":",(Object[])this.elements);
            if(Objects.isNull(ret)) ret = "null";
            else {
                if(Objects.nonNull(this.extra) && !this.extra.isEmpty()) ret += this.extra;
                ret = "<"+ret+">";
                if(this.amount>1) ret += "*" + this.amount;
            }
        }
        return ret;
    }
}

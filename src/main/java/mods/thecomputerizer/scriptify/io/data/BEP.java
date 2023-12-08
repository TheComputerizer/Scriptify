package mods.thecomputerizer.scriptify.io.data;

import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.oredict.MCOreDictEntry;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.scriptify.util.Patterns;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreIngredient;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Matcher;

public class BEP {

    public static BEP of(FluidStack stack) {
        return new BEP(stack.amount,"liquid",stack.getFluid().getName());
    }

    public static BEP of(ILiquidStack stack) {
        Fluid fluid = CraftTweakerMC.getFluid(stack.getDefinition());
        return of(new FluidStack(fluid,stack.getAmount()));
    }

    public static BEP of(ItemStack stack) {
        ResourceLocation resource = stack.getItem().getRegistryName();
        if(Objects.isNull(resource)) return new BEP();
        String name = resource.getNamespace();
        String path = resource.getPath();
        int meta = stack.getMetadata();
        return meta==0 ? new BEP(stack.getCount(),name,path) : new BEP(stack.getCount(),name,path,meta+"");
    }

    public static BEP of(IItemStack stack) {
        return of(CraftTweakerMC.getItemStack(stack));
    }

    public static BEP of(MCOreDictEntry oreDictEntry) {
        return new BEP("ore",oreDictEntry.getId());
    }

    public static BEP of(String str) {
        Matcher item = Patterns.BEP.matcher(str);
        Matcher amount = Patterns.AMOUNT.matcher(str);
        if(item.matches()) {
            String[] elements = item.group(1).split(":");
            return amount.matches() ? new BEP(Integer.parseInt(amount.group(1)),elements) : new BEP(elements);
        }
        return new BEP();
    }

    public static @Nullable BEP of(Object obj) {
        if(obj instanceof FluidStack) return of((FluidStack)obj);
        if(obj instanceof ILiquidStack) return of((ILiquidStack)obj);
        if(obj instanceof ItemStack) return of((ItemStack)obj);
        if(obj instanceof IItemStack) return of((IItemStack)obj);
        if(obj instanceof MCOreDictEntry) return of((MCOreDictEntry)obj);
        if(obj instanceof String) return of(obj.toString());
        return null;
    }

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

    public MCOreDictEntry asOreDictEntry() {
        if(this.amount==0 || Objects.isNull(this.elements) || this.elements.length<2) return null;
        return MCOreDictEntry.getFromIngredient(new OreIngredient(this.elements[1]));
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

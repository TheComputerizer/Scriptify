package mods.thecomputerizer.scriptify.io.write;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import lombok.Getter;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.io.data.BEP;
import mods.thecomputerizer.scriptify.io.data.ExpressionData;
import mods.thecomputerizer.scriptify.util.iterator.Wrapperable;
import mods.thecomputerizer.theimpossiblelibrary.util.object.ItemUtil;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Objects;

/**
 * Special writer instance that attempts to write data directly to the world, such as writing recipes to inventories
 */
@Getter
public class WorldWriter {

    private final ExpressionData dataInput;

    public WorldWriter(ExpressionData dataInput) {
        this.dataInput = dataInput;
    }

    public ItemStack writePrimitive(String asString, Class<?> clazz) {
        ItemStack paper = new ItemStack(Items.PAPER);
        paper.setStackDisplayName("primitive");
        NBTTagCompound tag = ItemUtil.getOrCreateTag(paper);
        NBTTagCompound primitiveTag = new NBTTagCompound();
        primitiveTag.setString("value",asString);
        primitiveTag.setString("className",clazz.getName());
        primitiveTag.setString("fromString",getParseFuncName(clazz.getSimpleName()));
        tag.setTag("primitiveData",primitiveTag);
        paper.setTagCompound(tag);
        return paper;
    }

    private String getParseFuncName(String simpleClassName) {
        simpleClassName = simpleClassName.trim().toLowerCase();
        if(simpleClassName.matches("byte")) return "parseByte";
        if(simpleClassName.matches("boolean")) return "parseBoolean";
        if(simpleClassName.matches("double")) return "parseDouble";
        if(simpleClassName.matches("float")) return "parseFloat";
        if(simpleClassName.matches("int") || simpleClassName.matches("integer")) return "parseInt";
        if(simpleClassName.matches("long")) return "parseLong";
        if(simpleClassName.matches("short")) return "parseShort";
        if(simpleClassName.matches("void")) return "void";
        return "toString";
    }

    private int writeIngredient(IInventory inventory, IIngredient ingredient, int slot) {
        if(ingredient instanceof IItemStack)
            return writeItemStack(inventory,CraftTweakerMC.getItemStack((IItemStack)ingredient),slot);
        if(ingredient instanceof ILiquidStack)
            return writeFluidStack(inventory,BEP.of((ILiquidStack)ingredient).asFluidStack(),slot);
        if(ingredient instanceof IOreDictEntry)
            return writeOreDict(inventory,(IOreDictEntry)ingredient,slot);
        return writePrimitive(inventory,ingredient,slot); //This shouldn't be reachable but other mods might have different IIngredient implementation
    }

    private int writeOreDict(IInventory inventory, IOreDictEntry entry, int slot) {
        ItemStack stack = CraftTweakerMC.getItemStack(entry.getFirstItem());
        if(stack.getMetadata()==OreDictionary.WILDCARD_VALUE) {
            int count = stack.getCount();
            stack = stack.getItem().getDefaultInstance();
            stack.setCount(count);
        }
        stack.setStackDisplayName(BEP.of(entry).toString());
        return writeItemStack(inventory,stack,slot);
    }

    private int writeFluidStack(IInventory inventory, FluidStack stack, int slot) {
        ItemStack paper = new ItemStack(Items.PAPER);
        paper.setStackDisplayName("liquid");
        NBTTagCompound tag = ItemUtil.getOrCreateTag(paper);
        NBTTagCompound liquidTag = new NBTTagCompound();
        liquidTag.setString("name",stack.getFluid().getName());
        liquidTag.setInteger("amount",stack.amount);
        tag.setTag("liquidData",liquidTag);
        paper.setTagCompound(tag);
        return writeItemStack(inventory,paper,slot);
    }

    private int writeItemStack(IInventory inventory, ItemStack stack, int slot) {
        inventory.setInventorySlotContents(slot,stack);
        return slot+1;
    }

    private int writePrimitive(IInventory inventory, Object primitive, int slot) {
        return writeItemStack(inventory,writePrimitive(primitive.toString(),primitive.getClass()),slot);
    }

    public void writeToInventory(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if(state.getBlock() instanceof BlockChest) {
            BlockChest chest = (BlockChest)state.getBlock();
            writeToInventory(chest.getLockableContainer(world,pos));
        }
    }

    public void writeToInventory(IInventory inventory) {
        int slot = 0;
        Wrapperable<ExpressionWriter> writers = this.dataInput.getWriters();
        for(int i=0; i<writers.size(); i++) {
            FileWriter writer = writers.get(i);
            if(Objects.nonNull(writer)) {
                Object value = writer.getValue(this.dataInput.getExpectedClassAt(i));
                ScriptifyRef.LOGGER.error("VALUE CLASS IS {} AND TOSTRING IS {}",value.getClass().getName(),value);
                slot = writeObject(inventory,value,slot);
            }
        }
    }

    public int writeObject(IInventory inventory, Object value, int slot) {
        if(value instanceof IIngredient) return writeIngredient(inventory,(IIngredient)value,slot);
        if(value instanceof Object[][]) return writeGrid(inventory,(Object[][])value,slot);
        if(value instanceof Object[]) return writeRow(inventory,(Object[])value,slot);
        return writePrimitive(inventory,value,slot);
    }

    private int writeGrid(IInventory inventory, Object[][] grid, int startingSlot) {
        for(Object[] row : grid) startingSlot = writeRow(inventory,row,startingSlot);
        return startingSlot;
    }

    /**
     * Hardcoded a width of 9 for testing purposes
     */
    private int writeRow(IInventory inventory, Object[] row, int startingSlot) {
        startingSlot = (startingSlot+9)-((startingSlot+1)%9)+1;
        for(Object element : row)
            startingSlot = writeObject(inventory,element,startingSlot);
        return startingSlot;
    }
}

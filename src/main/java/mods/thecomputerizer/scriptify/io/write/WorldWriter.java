package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import mods.thecomputerizer.scriptify.io.data.ExpressionData;
import mods.thecomputerizer.theimpossiblelibrary.util.object.ItemUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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

    public void writeToInventory() {

    }
}

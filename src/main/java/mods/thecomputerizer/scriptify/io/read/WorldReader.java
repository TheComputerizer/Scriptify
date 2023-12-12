package mods.thecomputerizer.scriptify.io.read;

import lombok.Getter;
import mods.thecomputerizer.scriptify.io.data.ExpressionData;
import mods.thecomputerizer.theimpossiblelibrary.util.file.DataUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.object.ItemUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.io.IOException;

/**
 * Special reader instance that attempts to read expressions from in-world data.
 * Generally useful for reading items from inventories
 */
@Getter
public class WorldReader {

    private final ExpressionData dataOutput;

    public WorldReader(ExpressionData dataOutput) {
        this.dataOutput = dataOutput;
    }

    private boolean isPrimitiveRepresentation(ItemStack stack) {
        if(stack.getItem()==Items.PAPER && stack.getDisplayName().trim().toLowerCase().matches("primitive")) {
            try {
                DataUtil.getOrCreateCompound(ItemUtil.getOrCreateTag(stack),"primitiveData");
                return true;
            } catch(IOException ignored) {}
        }
        return false;
    }
}

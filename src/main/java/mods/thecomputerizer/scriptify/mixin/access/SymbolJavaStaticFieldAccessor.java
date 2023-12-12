package mods.thecomputerizer.scriptify.mixin.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import stanhebben.zenscript.compiler.ITypeRegistry;
import stanhebben.zenscript.symbols.SymbolJavaStaticField;

import java.lang.reflect.Field;

@Mixin(value = SymbolJavaStaticField.class, remap = false)
public interface SymbolJavaStaticFieldAccessor {

    @Accessor Class<?> getCls();
    @Accessor Field getField();
    @Accessor ITypeRegistry getTypes();
}

package mods.thecomputerizer.scriptify.io.data;

import crafttweaker.api.item.IIngredient;
import crafttweaker.mc1120.oredict.MCOreDictEntry;
import lombok.Getter;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.write.FileWriter;
import mods.thecomputerizer.scriptify.io.write.MethodWriter;
import mods.thecomputerizer.scriptify.util.Misc;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class RecipeBlueprint {

    private static final Set<Class<?>> INGREDIENT_MATCHES = new HashSet<>(Arrays.asList(MCOreDictEntry.class,
            ItemStack.class,FluidStack.class));
    private static final Set<Class<?>> INGREDIENT_MATCHES_ARR_1 = new HashSet<>(Arrays.asList(MCOreDictEntry[].class,
            ItemStack[].class,FluidStack[].class));
    private static final Set<Class<?>> INGREDIENT_MATCHES_ARR_2 = new HashSet<>(Arrays.asList(MCOreDictEntry[][].class,
            ItemStack[][].class,FluidStack[][].class));
    private static final Set<Class<?>> INGREDIENT_MATCHES_ARR_3 = new HashSet<>(Arrays.asList(MCOreDictEntry[][][].class,
            ItemStack[][][].class,FluidStack[][][].class));

    public static boolean checkSpecialMatch(Class<?> parameter, Class<?> arg) {
        if(Number.class.isAssignableFrom(parameter)) return Number.class.isAssignableFrom(arg);
        if(parameter==IIngredient.class) return isSpecialMatch(arg,INGREDIENT_MATCHES);
        if(parameter==IIngredient[].class) return isSpecialMatch(arg,INGREDIENT_MATCHES_ARR_1);
        if(parameter==IIngredient[][].class) return isSpecialMatch(arg,INGREDIENT_MATCHES_ARR_2);
        if(parameter==IIngredient[][][].class) return isSpecialMatch(arg,INGREDIENT_MATCHES_ARR_3);
        return false;
    }

    private static boolean isSpecialMatch(Class<?> arg, Set<Class<?>> matches) {
        for(Class<?> clazz : matches)
            if(clazz.isAssignableFrom(arg))
                return true;
        return false;
    }

    @Getter private final String mod;
    @Getter private final String className;
    @Getter private final String methodName;
    private final DynamicArray[] parameterTypes;

    public RecipeBlueprint(String mod, String className, String methodName, String ... parameterTypeAliases) {
        this.mod = mod;
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parseParameterTypes(parameterTypeAliases);
    }

    public String getFirstTypeSimpleName() {
        if(this.parameterTypes.length==0) return "empty";
        DynamicArray type = this.parameterTypes[0];
        return type.getTypeClass().getSimpleName()+StringUtils.repeat("[",type.getBracketCount())+
                StringUtils.repeat("]",type.getBracketCount());
    }


    public boolean matches(String simpleClass, String otherMethod) {
        if(this.methodName.matches(otherMethod)) {
            String[] splitClass = this.className.split("\\.");
            return simpleClass.matches(splitClass[splitClass.length-1]);
        }
        return false;
    }

    private DynamicArray[] parseParameterTypes(String ... parameterTypeAliases) {
        DynamicArray[] types = new DynamicArray[parameterTypeAliases.length];
        for(int i=0; i<parameterTypeAliases.length; i++)
            types[i] = new DynamicArray(parameterTypeAliases[i]);
        return types;
    }

    @Override
    public String toString() {
        return this.className+"#"+this.methodName;
    }

    public boolean verifyArgs(List<Object> args) {
        for(int i=0; i<this.parameterTypes.length; i++) {
            Object arg = args.get(i);
            if(arg instanceof List<?>) arg = Misc.listToArray((List<?>)arg);
            DynamicArray parameter = this.parameterTypes[i];
            if(!parameter.isValid(arg)) {
                Scriptify.logError(getClass(),null,null,i,arg.getClass().getName(),parameter.getTypeClass().getName());
                return false;
            }
        }
        Scriptify.logDebug(getClass());
        return true;
    }

    public FileWriter makeWriter(Object ... args) {
        return makeWriter(Arrays.asList(args));
    }

    public FileWriter makeWriter(Collection<Object> args) {
        MethodWriter writer = new MethodWriter(0);
        writer.setClassName(this.className);
        writer.setMethodName(this.methodName);
        int i = 0;
        for(Object arg : args) {
            writer.addWriter(this.parameterTypes[i].makeWriter(arg));
            i++;
        }
        return writer;
    }
}

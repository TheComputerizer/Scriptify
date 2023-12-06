package mods.thecomputerizer.scriptify.io.data;

import crafttweaker.api.item.IIngredient;
import crafttweaker.mc1120.oredict.MCOreDictEntry;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.IOUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

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

    private final String className;
    private final String methodName;
    private final DynamicArray[] parameterTypes;

    public RecipeBlueprint(String className, String methodName, String ... parameterTypeAliases) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parseParameterTypes(parameterTypeAliases);
    }

    private DynamicArray[] parseParameterTypes(String ... parameterTypeAliases) {
        DynamicArray[] types = new DynamicArray[parameterTypeAliases.length];
        for(int i=0; i<parameterTypeAliases.length; i++)
            types[i] = new DynamicArray(parameterTypeAliases[i]);
        return types;
    }

    public boolean matches(String simpleClass, String otherMethod) {
        if(this.methodName.matches(otherMethod)) {
            String[] splitClass = this.className.split("\\.");
            return simpleClass.matches(splitClass[splitClass.length-1]);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.className+"#"+this.methodName;
    }

    public boolean verifyArgs(List<Object> args) {
        for(int i=0; i<this.parameterTypes.length; i++) {
            Object arg = args.get(i);
            if(arg instanceof List<?>) arg = IOUtils.listToArray((List<?>)arg);
            DynamicArray parameter = this.parameterTypes[i];
            if(!parameter.isValid(arg)) {
                Scriptify.logError("Argument at index {} of type {} did not match the expected blueprint of {}!",i,
                        arg.getClass().getName(),parameter.getTypeClass().getName());
                return false;
            } else
                Scriptify.logDebug("Successfully verified argument of class {} against parameter of class {}",
                        arg.getClass().getName(),parameter.getTypeClass().getName());
        }
        Scriptify.logDebug("Recipe has been sucessfully verified!");
        return true;
    }

    public String write(Object ... args) {
        StringBuilder builder = new StringBuilder(this.className+"."+this.methodName+"(");
        for(int i=0; i<args.length; i++) {
            //builder.append(IOUtils.getWriterFunc(this.args[i].getSimpleName()).apply(args[i]));
            if(i+1<args.length) builder.append(",");
        }
        return builder.append(")").toString();
    }
}

package mods.thecomputerizer.scriptify.io.data;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.oredict.IOreDictEntry;
import lombok.Getter;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.read.ExpressionReader;
import mods.thecomputerizer.scriptify.io.write.ClampedWriter;
import mods.thecomputerizer.scriptify.io.write.ExpressionWriter;
import mods.thecomputerizer.scriptify.io.write.FileWriter;
import org.apache.commons.lang3.StringUtils;
import stanhebben.zenscript.type.ZenType;

import java.util.*;

public class RecipeBlueprint {

    private static final Set<Class<?>> INGREDIENT_MATCHES = new HashSet<>(Arrays.asList(IOreDictEntry.class,
            IItemStack.class,ILiquidStack.class));
    private static final Set<Class<?>> INGREDIENT_MATCHES_ARR_1 = new HashSet<>(Arrays.asList(IOreDictEntry[].class,
            IItemStack[].class,ILiquidStack[].class));
    private static final Set<Class<?>> INGREDIENT_MATCHES_ARR_2 = new HashSet<>(Arrays.asList(IOreDictEntry[][].class,
            IItemStack[][].class,ILiquidStack[][].class));
    private static final Set<Class<?>> INGREDIENT_MATCHES_ARR_3 = new HashSet<>(Arrays.asList(IOreDictEntry[][][].class,
            IItemStack[][][].class,ILiquidStack[][][].class));

    public static boolean checkSpecialMatch(Class<?> parameter, Class<?> arg) {
        if(Number.class.isAssignableFrom(parameter)) return Number.class.isAssignableFrom(arg);
        if(parameter==IIngredient.class) return isSpecialMatch(parameter,INGREDIENT_MATCHES);
        if(parameter==IIngredient[].class) return isSpecialMatch(parameter,INGREDIENT_MATCHES_ARR_1);
        if(parameter==IIngredient[][].class) return isSpecialMatch(parameter,INGREDIENT_MATCHES_ARR_2);
        if(parameter==IIngredient[][][].class) return isSpecialMatch(parameter,INGREDIENT_MATCHES_ARR_3);
        return false;
    }

    private static boolean isSpecialMatch(Class<?> parameter, Set<Class<?>> matches) {
        for(Class<?> clazz : matches)
            if(parameter.isAssignableFrom(clazz))
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

    public boolean verifyArgs(List<ExpressionReader> readers) {
        for(int i=0; i<this.parameterTypes.length; i++) {
            ZenType type = readers.get(i).getType();
            DynamicArray parameter = this.parameterTypes[i];
            if(!parameter.isValid(type)) {
                Scriptify.logError(getClass(),null,null,i,type.getName(),parameter.getTypeClass().getName());
                return false;
            }
        }
        Scriptify.logDebug(getClass());
        return true;
    }

    public FileWriter makeWriter(Collection<ExpressionReader> readers) {
        ClampedWriter writer = new ClampedWriter(0);
        writer.setPrefix(this.className+"."+this.methodName);
        writer.setDisableSpaces(true);
        writer.setNewLine(true);
        writer.addWriters(reader -> {
            ExpressionWriter argWriter = new ExpressionWriter(reader.getExpression());
            argWriter.setTabLevel(1);
            return argWriter;
        },readers);
        return writer;
    }
}

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
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.StringUtils;
import stanhebben.zenscript.type.ZenType;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class Blueprint {

    private static final List<String> RELOADABLE_MODS = Arrays.asList("vanilla","extendedcrafting");
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

    private final boolean global;
    private final String className;
    private final String methodName;
    private final DynamicArray returnType;
    private final DynamicArray[] parameterTypes;
    private final int firstOptionalIndex;

    public Blueprint(boolean global, String className, String methodName, String returnType,
                     String ... parameterTypeAliases) {
        this.global = global;
        this.className = className;
        this.methodName = methodName;
        this.returnType = new DynamicArray(returnType);
        this.parameterTypes = parseParameterTypes(parameterTypeAliases);
        this.firstOptionalIndex = findOptionalStart();
    }

    private int findOptionalStart() {
        int optional = this.parameterTypes.length;
        if(optional>0) {
            for(int i=0; i<optional-1; i++) {
                if(this.parameterTypes[i].isOptional()) {
                    optional = i;
                    break;
                }
            }
        }
        return optional;
    }

    public @Nullable DynamicArray getParameterAt(int index) {
        return index<0 || index>=this.parameterTypes.length ? null : this.parameterTypes[index];
    }

    public String getFirstTypeSimpleName() {
        if(this.parameterTypes.length==0) return "empty";
        DynamicArray type = this.parameterTypes[0];
        return type.getTypeClass().getSimpleName()+StringUtils.repeat("[",type.getBracketCount())+
                StringUtils.repeat("]",type.getBracketCount());
    }

    public String getMod() {
        if(!this.className.contains(".")) return this.className;
        int count = StringUtils.countMatches(this.className,'.');
        if(count==1) return Misc.getLastSplit(this.className,".");
        String chopped = this.className.substring(this.className.indexOf('.')+1);
        return chopped.substring(0,chopped.indexOf('.')).trim().toLowerCase();
    }

    public String getTypeName() {
        return this.className+"#"+this.methodName;
    }

    public boolean isReloadable() {
        if(Loader.isModLoaded("zenrecipereloading")) {
            return RELOADABLE_MODS.contains(getMod());
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if(getClass()!=o.getClass()) return false;
        Blueprint other = (Blueprint)o;
        if(this.className.matches(other.className) && this.methodName.matches(other.methodName) &&
                this.returnType==other.returnType && this.parameterTypes.length==other.parameterTypes.length) {
            for(int i=0; i<this.parameterTypes.length; i++)
                if(this.parameterTypes[i]!=other.parameterTypes[i]) return false;
        } else return false;
        return true;
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
        String args = TextUtil.arrayToString(",",(Object[])this.parameterTypes);
        args = Objects.nonNull(args) ? args : "";
        return this.className+"#"+this.methodName+"("+args+")"+this.returnType;
    }

    public boolean verifyArgs(List<ExpressionReader> readers) {
        if(readers.size()<this.firstOptionalIndex) return false;
        if(!this.className.matches("unknown")) {
            for(int i=0; i<this.parameterTypes.length; i++) {
                if(i+1>readers.size() && i>=this.firstOptionalIndex) break;
                ZenType type = readers.get(i).getType();
                DynamicArray parameter = this.parameterTypes[i];
                if(!parameter.isValid(type)) {
                    Scriptify.logError(getClass(),null,null,i,type.getName(), parameter.getTypeClass().getName());
                    return false;
                }
            }
        }
        Scriptify.logDebug(getClass());
        return true;
    }

    public FileWriter makeWriter(Collection<ExpressionReader> readers) {
        ClampedWriter writer = new ClampedWriter(0);
        writer.setPrefix(Misc.getLastSplit(this.className,".") +"."+this.methodName);
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

package mods.thecomputerizer.scriptify.io;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.IngredientUnknown;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDict;
import crafttweaker.api.oredict.IOreDictEntry;
import crafttweaker.mc1120.oredict.MCOreDictEntry;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.data.BEP;
import mods.thecomputerizer.scriptify.io.write.FileWriter;
import mods.thecomputerizer.scriptify.io.write.PartialWriter;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.scriptify.util.Patterns;
import mods.thecomputerizer.scriptify.util.iterator.WrapperableMappable;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.type.ZenTypeFunction;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;

/**
 * Read/write util methods with some addition specific string stuff
 */
@SuppressWarnings("unchecked")
public class IOUtils {

    private static final WrapperableMappable<Class<?>,String> CLASS_ALIASES = new WrapperableMappable<>(new HashMap<>(),false); //case-insensitive
    private static final Map<String,Function<String,Object>> READER_MAP = new HashMap<>();
    private static final Map<String,Function<Object,FileWriter>> WRITER_MAP = new HashMap<>();

    public static void addBasicClassAliases(Class<?> ... classes) {
        for(Class<?> clazz : classes) addClassAliases(clazz);
    }

    public static void addClassAliases(Class<?> clazz, Object ... aliasArgs) {
        if(CLASS_ALIASES.containsKey(clazz)) return;
        List<Object> aliasArgList = new ArrayList<>(Arrays.asList(aliasArgs));
        aliasArgList.add(getClassNames(clazz));
        Set<String> aliasSet = new HashSet<>();
        for(Object arg : aliasArgList) {
            if(arg instanceof String) Misc.lowerCaseAddCollection(aliasSet,fixClassName((String)arg));
            else if(arg instanceof Tuple<?,?>) {
                Tuple<?,?> argTuple = (Tuple<?,?>)arg;
                Misc.lowerCaseAddCollection(aliasSet,fixClassName(argTuple.getFirst().toString()));
                Misc.lowerCaseAddCollection(aliasSet,fixClassName(argTuple.getSecond().toString()));
            } else if(arg instanceof String[]) {
                for(String argArrElement : (String[]) arg)
                    Misc.lowerCaseAddCollection(aliasSet,fixClassName(argArrElement));
            } else Misc.lowerCaseAddCollection(aliasSet,fixClassName(arg.toString()));
        }
        CLASS_ALIASES.putFast(clazz,aliasSet);
        Scriptify.logDebug(IOUtils.class,null,clazz.getName(),TextUtil.compileCollection(aliasSet));
    }

    private static String fixClassName(String className) {
        return className.toLowerCase().replaceFirst("crafttweaker.api","crafttweaker");
    }

    public static String getBaseTypeName(ZenType type) {
        if(Objects.isNull(type)) return "null";
        String name = type.getName();
        if(type instanceof ZenTypeFunction) name = ((ZenTypeFunction)type).getReturnType().getName();
        return name.replaceFirst("ZenTypeNative: ", "");
    }

    public static Class<?> getClassFromAlias(String alias) {
        return CLASS_ALIASES.getKeyOrDefault(w -> Patterns.matchesAny(alias,w),Object.class);
    }

    private static Tuple<String,String> getClassNames(Class<?> clazz) {
        return new Tuple<>(clazz.getName(),clazz.getSimpleName());
    }

    public static IIngredient getFirstOreDictEntry(ItemStack stack) {
        return getOreDictEntryFromIndex(stack,0);
    }

    /**
     * Returns IIngedient since an IngredientUnknown instance is returned when no entries are found
     */
    public static IIngredient getOreDictEntryFromIndex(ItemStack stack, int index) {
        Item item = stack.getItem();
        int meta = stack.getMetadata();
        IOreDictEntry entry = null;
        try {
            int[] oreIDs = OreDictionary.getOreIDs(stack);
            if(oreIDs.length==0) Scriptify.logError(IOUtils.class,"oredict",null,item.getRegistryName(),meta);
            else {
                index = index<0 ? 0 : (index>=oreIDs.length ? oreIDs.length-1 : index);
                entry = CraftTweakerMC.getOreDict(OreDictionary.getOreName(index));
            }
        } catch(IllegalArgumentException ex) {
            Scriptify.logError(IOUtils.class,"oredict",ex,item.getRegistryName(),meta);
        }
        return Objects.nonNull(entry) ? entry : IngredientUnknown.INSTANCE;
    }

    public static Function<String,Object> getReaderFunc(String name) {
        Matcher arrayMatcher = Patterns.ARRAY_DEF.matcher(name);
        boolean isArray = false;
        if(arrayMatcher.matches()) {
            isArray = true;
            name = arrayMatcher.replaceAll("");
        }
        if(Patterns.matchesAny(name,"bep","crafttweaker.item.IItemStack","IItemStack","IItemStack","ItemStack",
                "net.minecraft.item.ItemStack","Item","crafttweaker.liquid.ILiquidStack","ILiquidStack","FluidStack",
                "net.minecraftforge.fluids.FluidStack","Fluid","Liquid","crafttweaker.oredict.IOreDictEntry",
                "IOreDictEntry","OreDict","Ore","OreDictionary","IOreDict")) name = "bep";
        else name = "basic";
        if(isArray) name+="Array";
        return READER_MAP.getOrDefault(name,Object::toString);
    }

    public static <T> FileWriter getWriter(String name, T arg) {
        return getWriterFunc(getClassFromAlias(name)).apply(arg);
    }

    public static Function<Object,FileWriter> getWriterFunc(String name) {
        return getWriterFunc(getClassFromAlias(name));
    }


    public static Function<Object,FileWriter> getWriterFunc(Class<?> clazz) {
        String name = "basic";
        if(clazz==ItemStack.class) name = "item";
        else if(clazz==FluidStack.class) name = "liquid";
        else if(clazz==MCOreDictEntry.class) name = "ore";
        return WRITER_MAP.getOrDefault(name,obj -> new PartialWriter<>(0));
    }

    /**
     * Removes blank values from string collections
     */
    public static void lintCollections(Collection<String> ... collections) {
        for(Collection<String> c : collections) c.removeIf(StringUtils::isBlank);
    }

    public static void loadDefaults() {
        loadDefaultClassAliases();
        loadDefaultReaders();
        loadDefaultWriters();
    }

    private static void loadDefaultClassAliases() {
        Scriptify.logInfo(IOUtils.class);
        addBasicClassAliases(Byte.class,Character.class,Double.class,Float.class,Long.class,Short.class,String.class,Void.class);
        addClassAliases(Boolean.class,"bool");
        addClassAliases(Integer.class,"int");
        addClassAliases(IItemStack.class,getClassNames(ItemStack.class),"item");
        addClassAliases(ILiquidStack.class,getClassNames(FluidStack.class),"liquid","fluid");
        addClassAliases(IOreDictEntry.class,getClassNames(MCOreDictEntry.class),getClassNames(IOreDict.class),
                getClassNames(OreDictionary.class),"ore");
        addClassAliases(IIngredient.class,"ingredient","ingr","ing");
    }

    private static void loadDefaultReaders() {
        READER_MAP.put("bep",BEP::of);
    }

    private static void loadDefaultWriters() {
        WRITER_MAP.put("basic",obj -> {
            PartialWriter<?> writer = new PartialWriter<>();
            setWriterElement(writer,obj);
            return writer;
        });
        WRITER_MAP.put("item",obj -> {
            PartialWriter<ItemStack> writer = new PartialWriter<>();
            BEP bep = BEP.of(obj);
            writer.setElement(Objects.isNull(bep) ? ItemStack.EMPTY : bep.asItemStack());
            return writer;
        });
        WRITER_MAP.put("liquid",obj -> {
            PartialWriter<FluidStack> writer = new PartialWriter<>();
            BEP bep = BEP.of(obj);
            writer.setElement(Objects.isNull(bep) ? new FluidStack(FluidRegistry.WATER,0) : bep.asFluidStack());
            return writer;
        });
        WRITER_MAP.put("ore",obj -> {
            PartialWriter<IOreDictEntry> writer = new PartialWriter<>();
            BEP bep = BEP.of(obj);
            writer.setElement(Objects.isNull(bep) ? new MCOreDictEntry("logWood") : bep.asIOreDictEntry());
            return writer;
        });
    }

    /**
     * It's assumed that the instance array is not empry, the index array is not empty, and the generic type is the
     * correct superclass level
     */
    public static <E> E[] mapShapeless(E[] instances, int[] indices) {
        assert instances.length>0 && indices.length>0;
        E[] mapped = (E[])Array.newInstance(instances[0].getClass(),indices.length);
        for(int i=0; i<indices.length; i++) mapped[i] = instances[indices[i]];
        return mapped;
    }

    /**
     * 2D version of the mapper. Can be used to represent a generic shaped recipe
     */
    public static <E> E[][] map2DShaped(E[] instances, int[][] indices) {
        assert instances.length>0 && indices.length>0 && indices[0].length>0;
        E[][] mapped = (E[][])Array.newInstance(instances[0].getClass(),indices.length,indices[0].length);
        for(int i=0; i<indices.length; i++)
            for(int j=0; j<indices[j].length; j++)
                mapped[i][j] = instances[indices[i][j]];
        return mapped;
    }


    /**
     * 3D version of the mapper
     */
    public static <E> E[][][] map3DShaped(E[] instances, int[][][] indices) {
        assert instances.length>0 && indices.length>0 && indices[0].length>0 && indices[0][0].length>0;
        E[][][] mapped = (E[][][])Array.newInstance(instances[0].getClass(),indices.length,indices[0].length,indices[0][0].length);
        for(int i=0; i<indices.length; i++)
            for(int j=0; j<indices[j].length; j++)
                for(int k=0; k<indices[i][j].length; k++)
                    mapped[i][j][k] = instances[indices[i][j][k]];
        return mapped;
    }

    /**
     * Stupid syntax sugar
     */
    public static <T> void setWriterElement(PartialWriter<T> writer, Object element) {
        writer.setElement((T)element);
    }

    public static String writeOperator(OperatorType type) {
        switch(type) {
            case ADD: return "+";
            case SUB: return "-";
            case MUL: return "*";
            case DIV: return "/";
            case MOD: return "%";
            case CAT: return "&";
            case OR: return "||";
            case AND: return "&&";
            case XOR: return "^";
            case NOT: return "!";
            case EQUALS: return "=";
            default: return type.toString();
        }
    }
}

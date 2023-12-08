package mods.thecomputerizer.scriptify.util;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.oredict.IOreDict;
import crafttweaker.api.oredict.IOreDictEntry;
import crafttweaker.mc1120.oredict.MCOreDictEntry;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.data.BEP;
import mods.thecomputerizer.scriptify.io.read.PartialExpressionReader;
import mods.thecomputerizer.scriptify.io.write.FileWriter;
import mods.thecomputerizer.scriptify.io.write.PartialWriter;
import mods.thecomputerizer.scriptify.mixin.access.ExpressionIntAccessor;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionInt;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;

/**
 * Read/write util methods with some addition specific string stuff
 */
@SuppressWarnings("unchecked")
public class IOUtils {

    private static final Map<Class<?>,Set<String>> CLASS_ALIASES = new HashMap<>(); //case-insensitive
    private static final Map<String,Function<String,Object>> READER_MAP = new HashMap<>();
    private static final Map<String,Function<Object,FileWriter>> WRITER_MAP = new HashMap<>();
    private static final Map<String,Set<String>> RECIPE_TYPE_CACHE = new HashMap<>();

    public static void addBasicClassAliases(Class<?> ... classes) {
        for(Class<?> clazz : classes) addClassAliases(clazz);
    }

    public static void addClassAliases(Class<?> clazz, Object ... aliasArgs) {
        List<Object> aliasArgList = new ArrayList<>(Arrays.asList(aliasArgs));
        aliasArgList.add(getClassNames(clazz));
        HashSet<String> aliasSet = new HashSet<>();
        for(Object arg : aliasArgList) {
            if(arg instanceof String) Misc.lowerCaseAddCollection(aliasSet,((String)arg).replaceFirst("crafttweaker.api","crafttweaker"));
            else if(arg instanceof Tuple<?,?>) {
                Tuple<?,?> argTuple = (Tuple<?,?>)arg;
                Misc.lowerCaseAddCollection(aliasSet,argTuple.getFirst().toString().replaceFirst("crafttweaker.api","crafttweaker"));
                Misc.lowerCaseAddCollection(aliasSet,argTuple.getSecond().toString().replaceFirst("crafttweaker.api","crafttweaker"));
            } else if(arg instanceof String[]) {
                for(String argArrElement : (String[]) arg)
                    Misc.lowerCaseAddCollection(aliasSet,argArrElement.replaceFirst("crafttweaker.api","crafttweaker"));
            } else Misc.lowerCaseAddCollection(aliasSet,arg.toString().replaceFirst("crafttweaker.api","crafttweaker"));
        }
        CLASS_ALIASES.put(clazz,aliasSet);
        Scriptify.logDebug(IOUtils.class,null,clazz.getName(),TextUtil.compileCollection(aliasSet));
    }

    public static List<String> combineRecipeTypes(String prefix, String arg) {
        List<String> combined = new ArrayList<>();
        for(Map.Entry<String,Set<String>> classEntry : RECIPE_TYPE_CACHE.entrySet()) {
            String className = classEntry.getKey();
            for(String methodName : classEntry.getValue()) {
                if(className.matches("recipes")) methodName = className+"."+methodName;
                methodName = prefix+"="+methodName;
                if(methodName.startsWith(arg)) combined.add(methodName);
            }
        }
        return combined;
    }

    public static Class<?> getClassFromAlias(String alias) {
        Class<?> clazz = Object.class;
        for(Map.Entry<Class<?>,Set<String>> aliasEntry : CLASS_ALIASES.entrySet()) {
            if(Patterns.matchesAny(alias, aliasEntry.getValue())) {
                clazz = aliasEntry.getKey();
                break;
            }
        }
        return clazz;
    }

    private static Tuple<String,String> getClassNames(Class<?> clazz) {
        return new Tuple<>(clazz.getName(),clazz.getSimpleName());
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
        loadDefaultRecipeTypes();
        loadDefaultWriters();
    }

    private static void loadDefaultClassAliases() {
        Scriptify.logInfo(IOUtils.class);
        addBasicClassAliases(Byte.class,Character.class,Double.class,Float.class,Long.class,Short.class,String.class);
        addClassAliases(Boolean.class,"bool");
        addClassAliases(Integer.class,"int");
        addClassAliases(ItemStack.class,getClassNames(IItemStack.class),"item");
        addClassAliases(FluidStack.class,getClassNames(ILiquidStack.class),"liquid","fluid");
        addClassAliases(MCOreDictEntry.class,getClassNames(IOreDictEntry.class),getClassNames(IOreDict.class),
                getClassNames(OreDictionary.class),"ore");
        addClassAliases(IIngredient.class,"ingredient","ingr","ing");
    }

    private static void loadDefaultReaders() {
        READER_MAP.put("bep",BEP::of);
    }

    private static void loadDefaultRecipeTypes() {
        RECIPE_TYPE_CACHE.put("recipes",new HashSet<>());
        RECIPE_TYPE_CACHE.get("recipes").addAll(Arrays.asList("addShaped","addShapeless"));
        RECIPE_TYPE_CACHE.put("TableCrafting",new HashSet<>());
        RECIPE_TYPE_CACHE.get("TableCrafting").addAll(Arrays.asList("addShaped","addShapeless"));
    }

    private static void loadDefaultWriters() {
        WRITER_MAP.put("basic",obj -> {
            PartialWriter<?> writer = new PartialWriter<>();
            setWriterElement(writer,obj);
            return writer;
        });
        WRITER_MAP.put("item",obj -> {
            PartialWriter<ItemStack> writer = new PartialWriter<>();
            BEP bep = null;
            if(obj instanceof ItemStack || obj instanceof IItemStack) bep = BEP.of(obj);
            else if(obj instanceof Expression[]) {
                Expression[] args = (Expression[])obj;
                if(args.length>=2) {
                    ResourceLocation resource = new ResourceLocation(new PartialExpressionReader(args[0]).toString());
                    if(ForgeRegistries.ITEMS.containsKey(resource)) {
                        Item item = ForgeRegistries.ITEMS.getValue(resource);
                        int meta = Integer.parseInt(args[1] instanceof ExpressionInt ?
                                String.valueOf(((ExpressionIntAccessor)args[1]).getValue()) :
                                new PartialExpressionReader(args[1]).toString());
                        int count = 1;
                        assert Objects.nonNull(item);
                        bep = BEP.of(new ItemStack(item,count,meta));
                    }
                }
            }
            writer.setElement(Objects.isNull(bep) ? ItemStack.EMPTY : bep.asItem());
            return writer;
        });
        WRITER_MAP.put("liquid",obj -> {
            PartialWriter<FluidStack> writer = new PartialWriter<>();
            BEP bep = null;
            if(obj instanceof FluidStack || obj instanceof ILiquidStack) bep = BEP.of(obj);
            else if(obj instanceof Expression[]) {
                Expression[] args = (Expression[])obj;
                if(args.length>=1) bep = new BEP("liquid",new PartialExpressionReader(args[0]).toString());
            }
            writer.setElement(Objects.isNull(bep) ? new FluidStack(FluidRegistry.WATER,0) : bep.asFluid());
            return writer;
        });
        WRITER_MAP.put("ore",obj -> {
            PartialWriter<MCOreDictEntry> writer = new PartialWriter<>();
            BEP bep = null;
            if(obj instanceof MCOreDictEntry) bep = BEP.of(obj);
            else if(obj instanceof Expression[]) {
                Expression[] args = (Expression[])obj;
                if(args.length>=1) bep = new BEP("ore",new PartialExpressionReader(args[0]).toString());
            }
            writer.setElement(Objects.isNull(bep) ? new MCOreDictEntry("logWood") : bep.asOreDictEntry());
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

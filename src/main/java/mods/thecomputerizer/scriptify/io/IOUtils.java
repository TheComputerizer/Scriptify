package mods.thecomputerizer.scriptify.io;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import crafttweaker.api.oredict.IOreDict;
import crafttweaker.api.oredict.IOreDictEntry;
import crafttweaker.mc1120.oredict.MCOreDictEntry;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.data.BEP;
import mods.thecomputerizer.scriptify.io.data.RecipeBlueprint;
import mods.thecomputerizer.scriptify.io.read.PartialExpressionReader;
import mods.thecomputerizer.scriptify.mixin.access.ExpressionIntAccessor;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.StringUtils;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionInt;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read/write util methods with some addition specific string stuff
 */
public class IOUtils {

    public static final Pattern ARRAY_TYPE_PATTERN = Pattern.compile("\\[([\\[\\]]*)]");
    public static final Pattern BEP_PATTERN = Pattern.compile("<([a-z0-9_\\-:]+)>",Pattern.CASE_INSENSITIVE);
    public static final Pattern PARAMETER_PATTERN = Pattern.compile("([a-z0-9_\\-:\\[\\]]+)=([a-z0-9_\\-:\\[\\]]+)",Pattern.CASE_INSENSITIVE);
    private static final Map<Class<?>,Set<String>> CLASS_ALIASES = new HashMap<>(); //case-insensitive
    private static final Map<String,Function<String,Object>> READER_MAP = new HashMap<>();
    private static final Map<String,Function<Object,String>> WRITER_MAP = new HashMap<>();
    private static final Map<String,Set<String>> RECIPE_TYPE_CACHE = new HashMap<>();

    public static void addBasicClassAliases(Class<?> ... classes) {
        for(Class<?> clazz : classes) addClassAliases(clazz);
    }

    public static void addClassAliases(Class<?> clazz, Object ... aliasArgs) {
        List<Object> aliasArgList = new ArrayList<>(Arrays.asList(aliasArgs));
        aliasArgList.add(getClassNames(clazz));
        HashSet<String> aliasSet = new HashSet<>();
        for(Object arg : aliasArgList) {
            if(arg instanceof String) lowerCaseAddCollection(aliasSet,((String)arg).replaceFirst("crafttweaker.api","crafttweaker"));
            else if(arg instanceof Tuple<?,?>) {
                Tuple<?,?> argTuple = (Tuple<?,?>)arg;
                lowerCaseAddCollection(aliasSet,argTuple.getFirst().toString().replaceFirst("crafttweaker.api","crafttweaker"));
                lowerCaseAddCollection(aliasSet,argTuple.getSecond().toString().replaceFirst("crafttweaker.api","crafttweaker"));
            } else if(arg instanceof String[]) {
                for(String argArrElement : (String[]) arg)
                    lowerCaseAddCollection(aliasSet,argArrElement.replaceFirst("crafttweaker.api","crafttweaker"));
            } else lowerCaseAddCollection(aliasSet,arg.toString().replaceFirst("crafttweaker.api","crafttweaker"));
        }
        CLASS_ALIASES.put(clazz,aliasSet);
        Scriptify.logDebug("Added aliases for class {} - {}",clazz.getName(),TextUtil.compileCollection(aliasSet));
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
            if(matchesAny(alias, aliasEntry.getValue())) {
                clazz = aliasEntry.getKey();
                break;
            }
        }
        return clazz;
    }

    private static Tuple<String,String> getClassNames(Class<?> clazz) {
        return new Tuple<>(clazz.getName(),clazz.getSimpleName());
    }

    public static String getLastSplit(String str, String splitBy) {
        String[] split = str.split(splitBy);
        return split[split.length-1];
    }

    public static Function<String,Object> getReaderFunc(String name) {
        Matcher arrayMatcher = ARRAY_TYPE_PATTERN.matcher(name);
        boolean isArray = false;
        if(arrayMatcher.matches()) {
            isArray = true;
            name = arrayMatcher.replaceAll("");
        }
        if(matchesAny(name,"bep","crafttweaker.item.IItemStack","IItemStack","IItemStack","ItemStack",
                "net.minecraft.item.ItemStack","Item","crafttweaker.liquid.ILiquidStack","ILiquidStack","FluidStack",
                "net.minecraftforge.fluids.FluidStack","Fluid","Liquid","crafttweaker.oredict.IOreDictEntry",
                "IOreDictEntry","OreDict","Ore","OreDictionary","IOreDict")) name = "bep";
        else name = "basic";
        if(isArray) name+="Array";
        return READER_MAP.getOrDefault(name,Object::toString);
    }

    public static Matcher getMatcher(CharSequence str, String regex) {
        return getMatcher(str,regex,0);
    }

    public static Matcher getMatcher(CharSequence str, String regex, int patternFlags) {
        return Pattern.compile(regex,patternFlags).matcher(str);
    }

    public static Function<Object,String> getWriterFunc(String name) {
        return getWriterFunc(getClassFromAlias(name));
    }


    public static Function<Object,String> getWriterFunc(Class<?> clazz) {
        String name = "basic";
        if(clazz==ItemStack.class) name = "item";
        else if(clazz==FluidStack.class) name = "liquid";
        else if(clazz== MCOreDictEntry.class) name = "ore";
        return WRITER_MAP.getOrDefault(name,Object::toString);
    }

    public static <T> Object listToArray(List<T> list) {
        return listToArray(list,new ArrayList<>(Collections.singletonList(list.size())));
    }

    public static <T> Object listToArray(List<T> list, List<Integer> dimensions) {
        if(list.isEmpty()) return makeArray(Object.class,0);
        T first = list.get(0);
        if(first instanceof List<?>) {
            List<?> next = ((List<?>)first);
            dimensions.add(next.size());
            return listToArray(next,dimensions);
        } else return makeArray(first.getClass(),fixBoxedArray(dimensions.toArray(new Integer[0])));
    }

    public static void loadDefaults() {
        loadDefaultClassAliases();
        loadDefaultReaders();
        loadDefaultRecipeTypes();
        loadDefaultWriters();
    }

    private static void loadDefaultClassAliases() {
        Scriptify.logInfo("Loading default class aliases");
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
        READER_MAP.put("bep",IOUtils::parseBEP);
    }

    private static void loadDefaultRecipeTypes() {
        RECIPE_TYPE_CACHE.put("recipes",new HashSet<>());
        RECIPE_TYPE_CACHE.get("recipes").addAll(Arrays.asList("addShaped","addShapeless"));
        RECIPE_TYPE_CACHE.put("TableCrafting",new HashSet<>());
        RECIPE_TYPE_CACHE.get("TableCrafting").addAll(Arrays.asList("addShaped","addShapeless"));
    }

    private static void loadDefaultWriters() {
        WRITER_MAP.put("item",obj -> {
            if(obj instanceof ItemStack) {
                return writeItem((ItemStack)obj).toString();
            } else if(obj instanceof Expression[]) {
                Expression[] args = (Expression[])obj;
                if(args.length>=2) {
                    ResourceLocation resource = new ResourceLocation(new PartialExpressionReader(args[0]).toString());
                    if(args[1] instanceof ExpressionInt)
                        return writeItem(resource,(int)((ExpressionIntAccessor)args[1]).getValue(),1).toString();
                    else return new BEP(1,resource.getNamespace(),resource.getPath(),
                            new PartialExpressionReader(args[1]).toString()).toString();
                }
            }
            return "null";
        });
        WRITER_MAP.put("liquid",obj -> {
            if(obj instanceof FluidStack) {
                return writeLiquid((FluidStack)obj).toString();
            } else if(obj instanceof Expression[]) {
                Expression[] args = (Expression[])obj;
                if(args.length>=1) return writeLiquid(new PartialExpressionReader(args[0]).toString(),1).toString();
            }
            return "null";
        });
        WRITER_MAP.put("ore",obj -> {
            if(obj instanceof ItemStack) {
                return writeOredict((ItemStack)obj).toString();
            } else if(obj instanceof Expression[]) {
                Expression[] args = (Expression[])obj;
                if(args.length==1) return writeOredict(new PartialExpressionReader(args[0]).toString(),1).toString();
            }
            return "null";
        });
    }

    /**
     * Adds a trimmed lowercase string a collection after ensuring it is not null, empty, or blank
     */
    public static void lowerCaseAddCollection(Collection<String> c, String str) {
        if(StringUtils.isNotBlank(str)) c.add(str.trim().toLowerCase());
    }

    /**
     * Adds a trimmed lowercase string a map key after ensuring it is not null, empty, or blank
     */
    public static <V> void lowerCaseAddMap(Map<String,V> map, String str, V val) {
        if(StringUtils.isNotBlank(str)) map.put(str.trim().toLowerCase(),val);
    }

    public static int[] fixBoxedArray(Integer ... boxed) {
        int[] primitive = new int[boxed.length];
        for(int i=0; i<boxed.length; i++) primitive[i] = boxed[i];
        return primitive;
    }

    public static Object makeArray(Class<?> clazz, int ... dimensions) {
        int[] dims = new int[dimensions.length];
        System.arraycopy(dimensions,0,dims,0,dims.length);
        try {
            return Array.newInstance(clazz,dims);
        } catch (IllegalArgumentException | NegativeArraySizeException ex) {
            Scriptify.logError("Unable to create reference array of class {}",clazz.getName(),ex);
            return new Object();
        }
    }

    public static boolean matchesAny(String original, String ... matches) {
        return matchesAny(original,false,Arrays.asList(matches));
    }

    public static boolean matchesAny(String original, Collection<String> matches) {
        return matchesAny(original,false,matches);
    }

    public static boolean matchesAny(String original, boolean matchCase, String ... matches) {
        return matchesAny(original,matchCase,Arrays.asList(matches));
    }

    public static boolean matchesAny(String original, boolean matchCase, Collection<String> matches) {
        if(StringUtils.isBlank(original)) return true;
        original = matchCase ? original : original.toLowerCase();
        for(String match : matches) {
            if(StringUtils.isBlank(match)) return true;
            match = matchCase ? match : match.toLowerCase();
            if(original.matches(match)) return true;
        }
        return false;
    }

    public static BEP parseBEP(String unparsed) {
        return new BEP(unparsed.replaceAll("<","").replace("\\>","").split(":"));
    }

    public static BEP writeItem(ItemStack stack) {
        if(stack.isEmpty()) return new BEP(0);
        ResourceLocation resource = stack.getItem().getRegistryName();
        if(Objects.isNull(resource)) return new BEP(0);
        return writeItem(resource,stack.getMetadata(),stack.getCount());
    }

    public static BEP writeItem(ResourceLocation resource, int meta, int amount) {
        return meta>0 ? new BEP(amount,resource.getNamespace(),resource.getPath(),String.valueOf(meta)) :
                new BEP(amount,resource.getNamespace(),resource.getPath());
    }

    public static BEP writeLiquid(FluidStack stack) {
        return writeLiquid(stack.getFluid().getName(),stack.amount);
    }

    public static BEP writeLiquid(String fluidName, int amount) {
        return new BEP(amount,"liquid",fluidName);
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

    public static BEP writeOredict(ItemStack stack) {
        if(stack.isEmpty()) return new BEP(0);
        int[] ids = OreDictionary.getOreIDs(stack);
        if(ids.length==0) return new BEP(0);
        return writeOredict(OreDictionary.getOreName(ids[0]),stack.getCount());
    }

    public static BEP writeOredict(String name, int amount) {
        return new BEP(amount,"ore",name);
    }
}

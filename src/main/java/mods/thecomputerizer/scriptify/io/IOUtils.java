package mods.thecomputerizer.scriptify.io;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.data.BEP;
import mods.thecomputerizer.scriptify.io.read.PartialExpressionReader;
import mods.thecomputerizer.scriptify.mixin.access.ExpressionIntAccessor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.OperatorType;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionInt;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOUtils {

    private static final Pattern ARRAY_TYPE_PATTERN = Pattern.compile("\\[([\\[\\]]+)]");
    private static final Pattern BEP_PATTERN = Pattern.compile("<([a-z0-9_\\-:]+)>",Pattern.CASE_INSENSITIVE);
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("([a-z0-9_\\-:\\[\\]]+)=([a-z0-9_\\-:\\[\\]]+)",Pattern.CASE_INSENSITIVE);
    private static final Map<String,Function<String,Object>> READER_MAP = new HashMap<>();
    private static final Map<String,Function<Object,String>> WRITER_MAP = new HashMap<>();
    private static final Map<String,Set<String>> RECIPE_TYPE_CACHE = new HashMap<>();

    public static void loadDefaults() {
        loadDefaultReaders();
        loadDefaultRecipeTypes();
        loadDefaultWriters();
    }

    private static void loadDefaultReaders() {
        READER_MAP.put("bep",IOUtils::parseBEP);
        READER_MAP.put("bepArray",IOUtils::parseBEP);
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

    public static Matcher getArrayMatcher(String name) {
        return ARRAY_TYPE_PATTERN.matcher(name);
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

    public static Function<Object,String> getWriterFunc(String name) {
        if(matchesAny(name,"crafttweaker.item.IItemStack","IItemStack","IItemStack",
                "ItemStack","net.minecraft.item.ItemStack","Item")) name = "item";
        else if(matchesAny(name,"crafttweaker.liquid.ILiquidStack","ILiquidStack","FluidStack",
                "net.minecraftforge.fluids.FluidStack","Fluid","Liquid")) name = "liquid";
        else if(matchesAny(name,"crafttweaker.oredict.IOreDictEntry","IOreDictEntry","OreDict",
                "Ore","OreDictionary","IOreDict")) name = "ore";
        return WRITER_MAP.getOrDefault(name,Object::toString);
    }

    public static boolean matchesAny(String original, String ... matches) {
        return matchesAny(original,false,matches);
    }

    public static boolean matchesAny(String original, boolean matchCase, String ... matches) {
        original = matchCase ? original : original.toLowerCase();
        for(String match : matches) {
            match = matchCase ? match : match.toLowerCase();
            if(original.matches(match)) return true;
        }
        return false;
    }

    public static BEP parseBEP(String unparsed) {
        return new BEP(BEP_PATTERN.matcher(unparsed).group().split(":"));
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

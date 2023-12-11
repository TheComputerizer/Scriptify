package mods.thecomputerizer.scriptify.util;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Uncategorized util methods
 */
public class Misc {

    /**
     * Originally I had the bright idea of making log messages translateable... Until I remembered lang files are not
     * normally available on the server. I didn't really want to just revert the changes to the log system either, so
     * I guess this is going to be a thing now.
     */
    private static final Map<String,Map<String,String>> LOG_LANG_CACHE = initLogLang();

    private static void addLogKey(Map<String,String> map, String unparsed) {
        String[] split = unparsed.split("=",2);
        if(split.length==2) map.put(split[0],split[1]);
        else ScriptifyRef.LOGGER.error("Failed to add translation for log message |{}|",unparsed);
    }

    public static boolean allNonNull(Object ... objects) {
        return !anyNull(objects);
    }


    public static boolean allNull(Object ... objects) {
        return !anyNonNull(objects);
    }

    public static boolean anyNonNull(Object ... objects) {
        for(Object obj : objects)
            if(Objects.nonNull(obj)) return true;
        return false;
    }

    public static boolean anyNull(Object ... objects) {
        for(Object obj : objects)
            if(Objects.isNull(obj)) return true;
        return false;
    }

    /**
     * Exceptions need to be handled externally
     */
    public static <K,V> @Nullable V applyNullable(@Nullable K thing, Function<K,V> function) {
        return Objects.isNull(thing) ? null : function.apply(thing);
    }

    /**
     * Exceptions need to be handled externally
     */
    public static <K> void consumeNullable(@Nullable K thing, Consumer<K> conumer) {
        if(Objects.nonNull(thing)) conumer.accept(thing);
    }

    public static void applyCachedLangFiles(Map<String,String[]> map) {
        LOG_LANG_CACHE.clear();
        for(Map.Entry<String,String[]> entry : map.entrySet()) {
            Map<String,String> localeMap = new HashMap<>();
            for(String translation : entry.getValue()) {
                String[] split = translation.split("=",2);
                if(split.length==2) localeMap.put(split[0],split[1]);
            }
            LOG_LANG_CACHE.put(entry.getKey(),Collections.unmodifiableMap(localeMap));
        }
        LOG_LANG_CACHE.putIfAbsent("en_us",createDefualtLogLang());
    }

    /**
     * No I don't feel like manually separating these into 2 strings even though I can probably regex replace it.
     */
    private static Map<String,String> createDefualtLogLang() {
        Map<String,String> map = new HashMap<>();
        addLogKey(map,"log.scriptify.collectionbundle.debug.removal=Failed to remove element at index `%1$s` from collection");
        addLogKey(map,"log.scriptify.collectionbundle.exception=Unable to get collection type `%1$s` as `%2$s`");
        addLogKey(map,"log.scriptify.collectionbundle.warn=Cannot determine element class of empty or null filled collection! Generic object will be substituted");
        addLogKey(map,"log.scriptify.dynamicarray.error=Unable to find class for name {} in DynamicArray object! Was the type stored correctly?");
        addLogKey(map,"log.scriptify.dynamicarray.warn.null=Cannot check if null value can be assigned to type class %1$s! The type will be assumed invalid.");
        addLogKey(map,"log.scriptify.dynamicarray.warn.type=Type class is set to %1$s which will be assumed to be valid but may break things!");
        addLogKey(map,"log.scriptify.ioutils.debug=Added aliases for class %1$s - %2$s");
        addLogKey(map,"log.scriptify.ioutils.error=Unable to create reference array of class %1$s");
        addLogKey(map,"log.scriptify.ioutils.info=Loading default class aliases");
        addLogKey(map,"log.scriptify.recipeblueprint.debug=Recipe has been sucessfully verified!");
        addLogKey(map,"log.scriptify.recipeblueprint.error=Argument at index %1$s of type %2$s did not match the expected blueprint of %3$s!");
        addLogKey(map,"log.scriptify.recipedatahandler.info=Successfully located blueprint `%1$s`");
        addLogKey(map,"log.scriptify.recipedatahandler.error.class=Unable to find recipe blueprints matching any known method name of classes %1$s!");
        addLogKey(map,"log.scriptify.recipedatahandler.error.get=Unable to find matching recipe type for `%1$s#%2$s`!");
        addLogKey(map,"log.scriptify.recipedatahandler.error.match=Expression of class `%1$s` cannot be parsed into recipe data!");
        addLogKey(map,"log.scriptify.recipedatahandler.error.method=Unable to find recipe blueprints matching any known class name of methods %1$s!");
        addLogKey(map,"log.scriptify.scriptify.exception=Lang keys with no arguments are not supported!");
        addLogKey(map,"log.scriptify.scriptifyconfighelper.error.write=Cannot write cache to null or nonexistant file `%1$s`");
        addLogKey(map,"log.scriptify.scriptifyconfighelper.exception.arrays=Unable to cache arrays from file `%1$s`");
        addLogKey(map,"log.scriptify.scriptifyconfighelper.exception.field=Could not find field of name %1$s in class `%2$s`");
        addLogKey(map,"log.scriptify.scriptifyconfighelper.exception.instance=Could not get instance of field %1$s in class `%2$s`");
        addLogKey(map,"log.scriptify.scriptifyconfighelper.exception.lines=Unable to cache lines from file `%1$s`");
        addLogKey(map,"log.scriptify.scriptifyconfighelper.info.arrays=Cached %1$s arrays from file `%2$s`");
        addLogKey(map,"log.scriptify.scriptifyconfighelper.info.lines=Cached %1$s lines from file `%2$s`");
        addLogKey(map,"log.scriptify.statementreader.debug=Statement class is `%1$s`");
        addLogKey(map,"log.scriptify.subcmd.exception.get=An error occured when trying to collect a parameter!");
        addLogKey(map,"log.scriptify.subcmd.exception.parameters=Failed to parse parameter sets!");
        addLogKey(map,"log.scriptify.subcmd.exception.save1=Unable to save parameters values!");
        addLogKey(map,"log.scriptify.subcmd.exception.save2=Unable to save running command!");
        addLogKey(map,"log.scriptify.subcmd.error=Unable to get unknown parameter `%1$s` for sub command `%2$s`!");
        addLogKey(map,"log.scriptify.subcmdcopy.debug.parse=Skipping file `%1$s` that failed to parse correctly");
        addLogKey(map,"log.scriptify.subcmdcopy.error.data=Unknown sortBy type`%1$s`! Defaulting to `class`");
        addLogKey(map,"log.scriptify.subcmdcopy.exception.data=Caught an exception while trying to parse recipe data in file `%1$s`");
        addLogKey(map,"log.scriptify.subcmdcopy.info.data=Skipping errored or empty recipe data data in file `%1$s`");
        addLogKey(map,"log.scriptify.zenfilereader.debug.move=Moving to file `%1$s`");
        addLogKey(map,"log.scriptify.zenfilereader.debug.parse1=Skipping data that wasn't parsed correctly");
        addLogKey(map,"log.scriptify.zenfilereader.debug.parse2=Skipping non expression statemen");
        addLogKey(map,"log.scriptify.zenfilereader.debug.parse3=Parsed %1$s recipes from file `%2$s` (class `%3$s`)");
        addLogKey(map,"log.scriptify.zenfilereader.exception.constructor=Parsing script file `%1$s`");
        addLogKey(map,"log.scriptify.zenfilereader.exception.parse=The recipe data parser caught an error!");
        addLogKey(map,"log.scriptify.zenfilereader.info.constructor=Parsing script file `%1$s`");
        addLogKey(map,"log.scriptify.zenfilereader.info.copyFunction=Writing %1$s functions");
        addLogKey(map,"log.scriptify.zenfilereader.info.copyStatement=Writing %1$s statements");
        return Collections.unmodifiableMap(map); //No adding to the lang map after it is created
    }

    /**
     * Replaces file paths in a list that point to folders with a list of all files in that folder. File paths that
     * point to single files are still added file paths that don't exist are automatically removed
     */
    public static List<String> expandFilePaths(@Nullable List<String> filePaths) {
        List<String> files = new ArrayList<>();
        if(Objects.nonNull(filePaths)) {
            for(String filePath : filePaths) {
                File file = new File(filePath);
                if(file.exists()) {
                    if(file.isDirectory()) {
                        String[] nextFiles = file.list();
                        if(Objects.nonNull(nextFiles)) files.addAll(expandFilePaths(nextFiles));
                    } else files.add(filePath);
                }
            }
        }
        return files;
    }

    public static List<String> expandFilePaths(String ... filePaths) {
        return expandFilePaths(Arrays.asList(filePaths));
    }

    public static boolean extendsAny(Class<?> clazz, Class<?> ... extensions) {
        for(Class<?> extension : extensions)
            if(extension.isAssignableFrom(clazz)) return true;
        return false;
    }

    public static int[] fixBoxedArray(Integer ... boxed) {
        int[] primitive = new int[boxed.length];
        for(int i=0; i<boxed.length; i++) primitive[i] = boxed[i];
        return primitive;
    }

    public static <V> V getEither(boolean choice, V ifChoice, V notChoice) {
        return choice ? ifChoice : notChoice;
    }

    /**
     * if choice 1 else choice 2 else neither
     */
    public static <V> V getEitherOr(boolean choice1, boolean choice2, V ifChoice1, V ifChoice2, V neither) {
        return choice1 ? ifChoice1 : (choice2 ? ifChoice2 : neither);
    }

    /**
     * The returns arrays is be used as reference and any choice element not present at the index will be false.
     * Assumes returns will always be nonnull with at least 1 element
     */
    @SafeVarargs
    public static <V> V getEitherTrailing(boolean[] choices, V ... returns) {
        if(returns.length==1) return returns[0];
        for(int i=0; i+1<returns.length; i++) {
            boolean choice = choices.length>i && choices[i];
            if(choice) return returns[i];
        }
        return returns[returns.length-1];
    }

    public static String getLastSplit(String str, String splitBy) {
        return str.substring(str.lastIndexOf(splitBy)+1);
    }

    public static <N,V> V getNullable(@Nullable N nullable, V notNull, V isNull) {
        return getEither(Objects.nonNull(nullable),notNull,isNull);
    }

    private static Map<String,Map<String,String>> initLogLang() {
        Map<String,Map<String,String>> map = new HashMap<>();
        map.put("en_us",createDefualtLogLang());
        return map;
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
        } else return makeArray(first.getClass(), fixBoxedArray(dimensions.toArray(new Integer[0])));
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

    public static Object makeArray(Class<?> clazz, int ... dimensions) {
        int[] dims = new int[dimensions.length];
        System.arraycopy(dimensions,0,dims,0,dims.length);
        try {
            return Array.newInstance(clazz,dims);
        } catch (IllegalArgumentException | NegativeArraySizeException ex) {
            Scriptify.logError(IOUtils.class,null,ex,clazz.getName());
            return new Object();
        }
    }

    public static String makeLogKey(Class<?> clazz, String level, @Nullable String qualifier) {
        String className = clazz.getSimpleName().toLowerCase();
        Object[] args = Objects.nonNull(qualifier) ? new String[]{"log",ScriptifyRef.MODID,className,level,qualifier} :
                new String[]{"log",ScriptifyRef.MODID,className,level};
        return TextUtil.arrayToString(".",args);
    }

    public static String removeAll(String str, String ... removals) {
        for(String removal : removals) str = str.replaceAll(removal,"");
        return str;
    }

    public static <E> void supplyArray(E[] array, Function<Integer,E> func) {
        for(int i=0; i<array.length; i++) array[i] = func.apply(i);
    }

    public static <E,F> void supplyArray(E[] array, F thing, BiFunction<F,Integer,E> func) {
        for(int i=0; i<array.length; i++) array[i] = func.apply(thing,i);
    }

    /**
     * Emulates the format of normal translations from actual lang files
     */
    public static String translateLog(Class<?> clazz, String level, @Nullable String qualifier, Object ... args) {
        String key = makeLogKey(clazz,level,qualifier);
        String val = LOG_LANG_CACHE.get(ScriptifyConfigHelper.getLangDefault()).get(key);
        return Objects.nonNull(val) ? String.format(val,args) : key;
    }
}

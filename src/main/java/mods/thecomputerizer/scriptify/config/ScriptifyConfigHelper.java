package mods.thecomputerizer.scriptify.config;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.util.Tuple;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

@SuppressWarnings({"DataFlowIssue","ResultOfMethodCallIgnored","SameParameterValue","unchecked"})
public class ScriptifyConfigHelper {

    private static final Map<String,String> CACHED_COMMANDS = new HashMap<>();
    private static final Map<String,String[]> CACHED_PARAMETERS = new HashMap<>();
    private static final Map<String,String[]> CACHED_LOCALE = new HashMap<>();
    private static final Map<String,Field> CACHE_MAP_FIELDS_BY_NAME = new HashMap<>();
    public static final Map<String,CacheState> CACHE_STATE = initCacheStates();

    public static <T> void addToCache(String cacheType, String name, T value) {
        Map<String,T> map = getCacheMap(cacheType.trim().toLowerCase());
        queryCache(cacheType,map);
        String adjustedName = name;
        int counter = 1;
        while(map.containsKey(adjustedName)) {
            adjustedName = name+counter;
            counter++;
        }
        map.put(adjustedName,value);
        CACHE_STATE.computeIfPresent(cacheType,(key,state) -> state.writeCache(map));
    }


    public static @Nullable String buildCommand(String type) {
        queryCache("commands",CACHED_COMMANDS);
        String command = CACHED_COMMANDS.get(type);
        if(Objects.isNull(command)) return null;
        if(!command.startsWith("/")) command = "/"+command;
        return command;
    }

    public static <T> void cacheFileArrays(File file, Map<String,T> map) {
        map.clear();
        if(file.exists()) {
            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String setName = null;
                List<String> parameters = new ArrayList<>();
                String line = reader.readLine();
                while(Objects.nonNull(line)) {
                    if(line.contains("<")) {
                        setName = line.substring(0,line.indexOf("<")).split("=",2)[0].trim();
                        line = line.substring(line.indexOf("<"));
                    }
                    boolean endOfSet = false;
                    if(line.contains(">")) {
                        endOfSet = true;
                        line = line.substring(0,line.indexOf(">")).trim();
                    }
                    if(line.contains("=")) parameters.add(line.trim());
                    if(endOfSet && Objects.nonNull(setName)) {
                        map.put(setName,(T)parameters.toArray(new String[0]));
                        parameters.clear();
                    }
                    line = reader.readLine();
                }
            } catch(IOException ex) {
                Scriptify.logError(ScriptifyConfigHelper.class,"arrays",ex,file.getName());
            }
        }
        Scriptify.logInfo(ScriptifyConfigHelper.class,"arrays",map.size(),file.getName());
    }

    public static <T> void cacheFileMap(File file, Map<String,T> map) {
        map.clear();
        if(file.exists()) {
            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                while(Objects.nonNull(line)) {
                    if(line.contains("=")) {
                        String[] split = line.split("=",2);
                        map.put(split[0].trim(),(T)split[1].trim());
                    }
                    line = reader.readLine();
                }
            } catch(IOException ex) {
                Scriptify.logError(ScriptifyConfigHelper.class,"lines",ex,file.getName());
            }
        }
        Scriptify.logInfo(ScriptifyConfigHelper.class,"lines",map.size(),file.getName());
    }

    public static <T> void cacheFolder(File folder, Map<String,T> map) {
        map.clear();
        if(folder.exists()) {
            File[] files = folder.listFiles();
            if(Objects.nonNull(files)) {
                for(File file : files) {
                    String name = file.getName();
                    try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line = reader.readLine();
                        List<String> validLines = new ArrayList<>();
                        while(Objects.nonNull(line)) {
                            if(line.contains("=")) validLines.add(line.trim());
                            line = reader.readLine();
                        }
                        map.put(name,(T)validLines.toArray(new String[0]));
                    } catch(IOException ex) {
                        Scriptify.logError(ScriptifyConfigHelper.class,"folder",ex,file.getName());
                    }
                }
            }
        }
        Scriptify.logInfo(ScriptifyConfigHelper.class,"folder",map.size(),folder.getName());
    }

    private static @Nullable Field findCachedField(String name) {
        name = "CACHED_"+name.trim().toUpperCase();
        try {
            return ScriptifyConfigHelper.class.getDeclaredField(name);
        } catch(NoSuchFieldException ex) {
            Scriptify.logError(ScriptifyConfigHelper.class,"field",ex,name,ScriptifyConfigHelper.class.getName());
            return null;
        }
    }

    private static @Nullable Object findFieldInstance(@Nullable Field field) {
        return Misc.applyNullable(field,f -> {
            try {
                return f.get(null);
            } catch(IllegalAccessException ex) {
                Scriptify.logError(ScriptifyConfigHelper.class,"instance",ex,f.getName(),f.getDeclaringClass());
                return null;
            }
        });
    }

    public static List<String> getCachedCommandNames(String prefix, String arg) {
        queryCache("commands",CACHED_COMMANDS);
        List<String> ret = new ArrayList<>();
        for(String command : CACHED_COMMANDS.keySet())
            if(arg.isEmpty() || command.startsWith(arg))
                ret.add(prefix+"="+command);
        return ret;
    }

    public static List<String> getCachedParameterSetNames(String prefix, String arg) {
        queryCache("parameters", CACHED_PARAMETERS);
        List<String> ret = new ArrayList<>();
        for(String parameters : CACHED_PARAMETERS.keySet())
            if(arg.isEmpty() || parameters.startsWith(arg))
                ret.add(prefix+"="+parameters);
        return ret;
    }

    public static <T> @Nullable Map<String,T> getCacheMap(String name) {
        Object instance = findFieldInstance(CACHE_MAP_FIELDS_BY_NAME.get(name));
        return Misc.getNullable(instance,(Map<String,T>)instance,null);
    }

    public static String getDefaultParameter(Collection<String> parameterSets, String name) {
        String ret = "";
        for(String set : parameterSets) {
            String[] parameters = getParameterSet(set);
            if(Objects.nonNull(parameters)) ret = getDefaultParameter(name,parameters);
            if(!ret.isEmpty()) break;
        }
        if(ret.isEmpty()) ret = getDefaultParameter(name,ScriptifyConfig.PARAMETERS.defaultParameterValues);
        return ret.contains("=") ? ret.split("=",2)[1] : ret;
    }

    private static String getDefaultParameter(String name, String[] parameterSet) {
        for(String parameter : parameterSet)
            if(parameter.startsWith(name+"=")) return parameter;
        return "";
    }

    public static String getLangDefault() {
        //queryCache("locale",CACHED_LOCALE);
        return ScriptifyConfig.MISC.logLangDefault;
    }

    public static String[] getParameterSet(String name) {
        queryCache("parameters", CACHED_PARAMETERS);
        return CACHED_PARAMETERS.get(name);
    }

    private static Map<String,CacheState> initCacheStates() {
        Map<String,CacheState> map = new HashMap<>();
        initCache(map,"commands",new CacheState(ScriptifyConfigHelper::cacheFileMap,"Commands",
                "commandBuilders"));
        initCache(map,"parameters",new CacheState(ScriptifyConfigHelper::cacheFileArrays,"Parameters",
                "parameters"));
        initCache(map,"locale",new CacheState(ScriptifyConfigHelper::cacheFolder,"Misc",
                "logLangFolder"));
        return map;
    }

    private static void initCache(Map<String,CacheState> stateMap, String name, CacheState state) {
        stateMap.put(name,state);
        CACHE_MAP_FIELDS_BY_NAME.put(name,findCachedField(name));
    }

    public static void onConfigReloaded() {
        for(Map.Entry<String,CacheState> entry : CACHE_STATE.entrySet()) {
            CACHED_COMMANDS.clear();
            CACHED_PARAMETERS.clear();
            entry.getValue().resetCache();
        }
    }

    public static void queryCache(String type, Map<String,?> cacheMap) {
        CacheState state = CACHE_STATE.get(type);
        if(Objects.nonNull(state)) state.applyConsumer(type,cacheMap);
    }

    public static final class CacheState {

        private final AtomicBoolean state;
        private final BiConsumer<File,Map<String,?>> consumer;
        private final Tuple<Field,Field> configFileFields;
        private File cachedFile;

        public CacheState(BiConsumer<File,Map<String,?>> consumer, String categoryField, String fileField) {
            this.state = new AtomicBoolean(true);
            this.consumer = consumer;
            this.configFileFields = getConfigFields(Misc.getInnerClass(ScriptifyConfig.class,categoryField),categoryField,fileField);
        }

        public void applyConsumer(String type, Map<String,?> cacheMap) {
            if(needsCache()) {
                this.consumer.accept(getFile(),cacheMap);
                if(type.matches("locale")) Misc.applyCachedLangFiles((Map<String,String[]>)cacheMap);
            }
        }

        private void cacheFile() {
            Object pathObj = getSecondFieldInstance(this.configFileFields);
            if(pathObj instanceof String) {
                String path = (String)pathObj;
                File file = Scriptify.getConfigFile(path);
                if(!path.endsWith(".txt")) file.mkdirs();
                else this.cachedFile = FileUtil.generateNestedFile(file,false);
            }
        }

        private Tuple<Field,Field> getConfigFields(Class<?> categoryClass, String categoryField, String fileField) {
            if(Misc.anyNull(categoryClass,categoryField,fileField)) return null;
            Field category = Misc.getField(ScriptifyConfig.class,categoryField.toUpperCase());
            if(Objects.nonNull(category)) {
                Field filePath = Misc.getField(categoryClass,fileField);
                if(Objects.nonNull(filePath)) return new Tuple<>(category,filePath);
            }
            return null;
        }

        private File getFile() {
            if(Objects.isNull(this.cachedFile) || !this.cachedFile.exists()) cacheFile();
            return this.cachedFile;
        }

        private @Nullable Object getSecondFieldInstance(@Nullable Tuple<Field,Field> fields) {
            if(Objects.isNull(fields)) return null;
            return Misc.getFieldInstance(Misc.getFieldInstance(null,fields.getFirst()),fields.getSecond());
        }

        private boolean needsCache() {
            if(this.state.getAndSet(false)) {
                cacheFile();
                return true;
            }
            return false;
        }

        private void resetCache() {
            this.state.set(true);
        }

        private <T> CacheState writeCache(Map<String,T> cacheMap) {
            if(Objects.isNull(this.cachedFile) || !this.cachedFile.exists()) {
                Scriptify.logError(ScriptifyConfigHelper.class,"write",null,this.cachedFile);
                return this;
            }
            List<String> lines = new ArrayList<>();
            List<String> names = new ArrayList<>(cacheMap.keySet());
            Collections.sort(names);
            for(String name : names) writeGeneric(lines,name,cacheMap.get(name));
            if(!lines.isEmpty()) lines.remove(lines.size()-1);
            FileUtil.writeLinesToFile(this.cachedFile,lines,false);
            this.state.set(true);
            return this;
        }

        private <T> void writeGeneric(List<String> lines, String name, T generic) {
            if(generic instanceof String[]) {
                lines.add(name+" <");
                List<String> valList = new ArrayList<>(Arrays.asList((String[])generic));
                Collections.sort(valList);
                for(String element : valList) lines.add("\t"+element);
                lines.add(">");
            } else lines.add(name+" = "+generic.toString());
            lines.add("");
        }
    }
}

package mods.thecomputerizer.scriptify.config;

import mods.thecomputerizer.scriptify.Scriptify;
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

@SuppressWarnings("unchecked")
public class ScriptifyConfigHelper {

    private static final Map<String,String> CACHED_COMMANDS = new HashMap<>();
    private static final Map<String,String[]> CACHED_PARAMETER_SETS = new HashMap<>();
    public static final Map<String,CacheState> CACHE_STATE = initCache();

    public static <T> void addToCache(String cacheType, String name, T value) {
        Map<String,T> map = cacheType.trim().toLowerCase().matches("commands") ? (Map<String,T>)CACHED_COMMANDS :
                (Map<String,T>)CACHED_PARAMETER_SETS;
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
                Scriptify.logError("Unable to cache arrays from file {}!",file.getName(),ex);
            }
        }
        Scriptify.logInfo("Cached {} arrays from file {}",map.size(),file.getName());
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
                Scriptify.logError("Unable to cache lines from file {}!",file.getName(),ex);
            }
        }
        Scriptify.logInfo("Cached {} lines from file {}",map.size(),file.getName());
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
        queryCache("parameters",CACHED_PARAMETER_SETS);
        List<String> ret = new ArrayList<>();
        for(String parameters : CACHED_PARAMETER_SETS.keySet())
            if(arg.isEmpty() || parameters.startsWith(arg))
                ret.add(prefix+"="+parameters);
        return ret;
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

    public static String[] getParameterSet(String name) {
        queryCache("parameters",CACHED_PARAMETER_SETS);
        return CACHED_PARAMETER_SETS.get(name);
    }

    private static Map<String,CacheState> initCache() {
        Map<String,CacheState> ret = new HashMap<>();
        ret.put("commands",new CacheState(ScriptifyConfigHelper::cacheFileMap,"Commands","commandBuilders"));
        ret.put("parameters",new CacheState(ScriptifyConfigHelper::cacheFileArrays,"Parameters","parameters"));
        return ret;
    }

    public static void onConfigReloaded() {
        for(Map.Entry<String,CacheState> entry : CACHE_STATE.entrySet()) {
            CACHED_COMMANDS.clear();
            CACHED_PARAMETER_SETS.clear();
            entry.getValue().resetCache();
        }
    }

    public static void queryCache(String type, Map<String,?> cacheMap) {
        CacheState state = CACHE_STATE.get(type);
        if(Objects.nonNull(state)) state.applyConsumer(cacheMap);
    }

    @SuppressWarnings("SameParameterValue")
    public static final class CacheState {

        private final AtomicBoolean state;
        private final BiConsumer<File,Map<String,?>> consumer;
        private final Tuple<Field,Field> configFileFields;
        private File cachedFile;

        public CacheState(BiConsumer<File,Map<String,?>> consumer, String categoryField, String fileField) {
            this.state = new AtomicBoolean(true);
            this.consumer = consumer;
            this.configFileFields = getConfigFields(getInnerClass(ScriptifyConfig.class,categoryField),categoryField,fileField);
        }

        public void applyConsumer(Map<String,?> cacheMap) {
            if(needsCache()) this.consumer.accept(getFile(),cacheMap);
        }

        private void cacheFile() {
            Object category = getFieldInstance(null,this.configFileFields.getFirst());
            if(Objects.nonNull(category)) {
                Object pathObj = getFieldInstance(category,this.configFileFields.getSecond());
                if(pathObj instanceof String) {
                    File file = Scriptify.getConfigFile((String)pathObj);
                    this.cachedFile = FileUtil.generateNestedFile(file,false);
                }
            }
        }

        private Tuple<Field,Field> getConfigFields(@Nullable Class<?> categoryClass, String categoryField, String fileField) {
            Field category = getField(ScriptifyConfig.class,categoryField.toUpperCase());
            if(Objects.nonNull(category)) {
                Field filePath = getField(categoryClass,fileField);
                if(Objects.nonNull(filePath)) return new Tuple<>(category,filePath);
            }
            return null;
        }

        private Field getField(@Nullable Class<?> clazz, String fieldName) {
            if(Objects.isNull(clazz)) return null;
            try {
                return clazz.getDeclaredField(fieldName);
            } catch(NoSuchFieldException ex) {
                Scriptify.logError("Could not find file of name {} in class {}!",fieldName,clazz,ex);
                return null;
            }
        }

        private File getFile() {
            if(Objects.isNull(this.cachedFile) || !this.cachedFile.exists()) cacheFile();
            return this.cachedFile;
        }

        private Object getFieldInstance(@Nullable Object instance, Field field) {
            try {
                return field.get(instance);
            } catch(IllegalAccessException ex) {
                Scriptify.logError("Could not get instance of field {} in class {}!",field.getName(),
                        field.getDeclaringClass(),ex);
                return null;
            }
        }

        private Class<?> getInnerClass(@Nullable Class<?> clazz, String name) {
            if(Objects.isNull(clazz)) return null;
            for(Class<?> categoryClass : clazz.getDeclaredClasses())
                if(categoryClass.getSimpleName().matches(name)) return categoryClass;
            return null;
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
                Scriptify.logError("Cannot write cache to null or nonexistant file {}!",this.cachedFile);
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

package mods.thecomputerizer.scriptify.config;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ScriptifyConfigHelper {

    private static final Map<String,String[]> CACHED_PARAMETER_SETS = new HashMap<>();
    public static boolean needsCache = true;

    public static void onConfigReloaded() {
        CACHED_PARAMETER_SETS.clear();
        needsCache = true;
    }

    public static String getDefaultParameter(Collection<String> parameterSets, String name) {
        String ret = "";
        for(String set : parameterSets) {
            String[] parameters = getParameterSet(set);
            if(Objects.nonNull(parameters)) ret = getDefaultParameter(name,parameters);
            if(!ret.isEmpty()) break;
        }
        if(ret.isEmpty()) ret = getDefaultParameter(name,ScriptifyConfig.PARAMETERS.defaultParameterValues);
        Scriptify.logInfo("ret is {}",ret);
        return ret.contains("=") ? ret.split("=",2)[1] : ret;
    }

    private static String getDefaultParameter(String name, String[] parameterSet) {
        for(String parameter : parameterSet)
            if(parameter.startsWith(name+"=")) return parameter;
        return "";
    }

    public static String[] getParameterSet(String name) {
        if(needsCache) {
            cacheParameterSets();
            needsCache = false;
        }
        return CACHED_PARAMETER_SETS.get(name);
    }

    public static File getParametersFile() {
        return FileUtil.generateNestedFile(Scriptify.getConfigFile(ScriptifyConfig.PARAMETERS.parameters),false);
    }

    public static void cacheParameterSets() {
        File parametersFile = getParametersFile();
        if(parametersFile.exists()) {
            try(BufferedReader reader = new BufferedReader(new FileReader(parametersFile))) {
                String setName = null;
                List<String> parameters = new ArrayList<>();
                String line = reader.readLine();
                while(Objects.nonNull(line)) {
                    if(line.contains("<")) {
                        setName = line.substring(0,line.indexOf("<")).split("=")[0].trim();
                        line = line.substring(line.indexOf("<"));
                    }
                    boolean endOfSet = false;
                    if(line.contains(">")) {
                        endOfSet = true;
                        line = line.substring(0,line.indexOf(">")).trim();
                    }
                    if(line.contains("=")) parameters.add(line.trim());
                    if(endOfSet && Objects.nonNull(setName)) {
                        CACHED_PARAMETER_SETS.put(setName,parameters.toArray(new String[0]));
                        Scriptify.logInfo("Cached parameter set {} with values {}",setName,
                                TextUtil.arrayToString(",",(Object[])CACHED_PARAMETER_SETS.get(setName)));
                        parameters.clear();
                    }
                    line = reader.readLine();
                }
            } catch(IOException ex) {
                Scriptify.logError("Unable to cache parameters sets!",ex);
            }
        }
    }
}

package mods.thecomputerizer.scriptify.config;

public class ConfigManager {

    public static String getDefault(String name) {
        for(String parameter : ScriptifyConfig.COMMANDS.defaultParameterValues)
            if(parameter.startsWith(name+"=")) return parameter;
        return "";
    }
}

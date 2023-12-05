package mods.thecomputerizer.scriptify.command.parameters.types;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterArray;
import mods.thecomputerizer.scriptify.command.parameters.common.ParameterString;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;
import mods.thecomputerizer.scriptify.io.IOUtils;

import java.util.Collections;
import java.util.List;

public class ParameterParameters extends ParameterArray<String,ParameterString> {

    public ParameterParameters() {
        super(Type.PARAMETER_PARAMETERS,ParameterString::new);
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        String name = this.getName();
        String arg = args[0].contains("=") ? args[0].split("=",2)[1] : args[0];
        return ScriptifyConfigHelper.getCachedParameterSetNames(name,arg);
    }
}

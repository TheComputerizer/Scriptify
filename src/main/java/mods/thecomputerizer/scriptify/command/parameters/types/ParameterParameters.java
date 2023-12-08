package mods.thecomputerizer.scriptify.command.parameters.types;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterStringArray;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;

import java.util.List;

public class ParameterParameters extends ParameterStringArray {

    public ParameterParameters() {
        super(Type.PARAMETER_PARAMETERS);
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        String name = this.getName();
        String arg = args[0];
        return ScriptifyConfigHelper.getCachedParameterSetNames(name,arg);
    }
}

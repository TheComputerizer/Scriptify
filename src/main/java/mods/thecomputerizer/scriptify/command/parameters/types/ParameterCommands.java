package mods.thecomputerizer.scriptify.command.parameters.types;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterStringArray;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;

import java.util.List;

public class ParameterCommands extends ParameterStringArray {

    public ParameterCommands() {
        super(Type.PARAMETER_COMMANDS);
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        String name = this.getName();
        String arg = args[0];
        return ScriptifyConfigHelper.getCachedCommandNames(name,arg);
    }
}

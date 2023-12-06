package mods.thecomputerizer.scriptify.command.parameters.types;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterArray;
import mods.thecomputerizer.scriptify.command.parameters.common.ParameterString;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;

import java.util.List;

public class ParameterCommands extends ParameterArray<String,ParameterString> {

    public ParameterCommands() {
        super(Type.PARAMETER_COMMANDS,ParameterString::new);
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        String name = this.getName();
        String arg = args[0];
        return ScriptifyConfigHelper.getCachedCommandNames(name,arg);
    }
}

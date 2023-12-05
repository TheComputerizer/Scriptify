package mods.thecomputerizer.scriptify.command.parameters.types;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterString;
import mods.thecomputerizer.scriptify.command.subcmd.SubCmdHelp;
import mods.thecomputerizer.scriptify.config.ScriptifyConfigHelper;
import mods.thecomputerizer.scriptify.io.IOUtils;

import java.util.Collections;
import java.util.List;

public class ParameterType extends ParameterString {

    public ParameterType() {
        super(Type.PARAMETER_TYPE);
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        String name = this.getName();
        String arg = args[0].contains("=") ? args[0].split("=",2)[1] : args[0];
        switch(this.runningType) {
            case COMMAND_HELP: return SubCmdHelp.getTypes(name,arg);
            case COMMAND_RECIPE: return IOUtils.combineRecipeTypes(name,arg);
            case COMMAND_RUN: return ScriptifyConfigHelper.getCachedCommandNames(name,arg);
            default: return Collections.singletonList(name+"=");
        }
    }
}

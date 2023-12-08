package mods.thecomputerizer.scriptify.command.parameters.types;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterStringArray;

import java.util.ArrayList;
import java.util.List;

public class ParameterQuery extends ParameterStringArray {

    public ParameterQuery() {
        super(Type.PARAMETER_QUERY);
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        List<String> ret = new ArrayList<>();
        String name = this.getName();
        String arg = args[0];
        for(Type type : Type.values())
            if(type.name.startsWith(arg)) ret.add(name+"="+type.name);
        return ret;
    }
}

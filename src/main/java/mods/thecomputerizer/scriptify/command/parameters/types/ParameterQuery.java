package mods.thecomputerizer.scriptify.command.parameters.types;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterArray;
import mods.thecomputerizer.scriptify.command.parameters.common.ParameterString;
import java.util.ArrayList;
import java.util.List;

public class ParameterQuery extends ParameterArray<String,ParameterString> {

    public ParameterQuery() {
        super(Type.PARAMETER_QUERY,ParameterString::new);
    }

    @Override
    public List<String> getTabCompletions(String... args) {
        List<String> ret = new ArrayList<>();
        String name = this.getName();
        String arg = args[0].contains("=") ? args[0].split("=",2)[1] : args[0];
        for(Type type : Type.values())
            if(type.name.startsWith(arg)) ret.add(name+"="+type.name);
        return ret;
    }
}

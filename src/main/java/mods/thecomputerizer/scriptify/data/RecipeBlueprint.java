package mods.thecomputerizer.scriptify.data;

import mods.thecomputerizer.scriptify.io.IOUtils;

import javax.annotation.Nullable;
import java.util.*;

public class RecipeBlueprint {

    private static final Map<String,List<RecipeBlueprint>> METHODS_BY_CLASS = new HashMap<>();

    public static @Nullable RecipeBlueprint getBlueprint(String className, String methodName) {
        List<RecipeBlueprint> methods = METHODS_BY_CLASS.get(className);
        if(Objects.nonNull(methods))
            for(RecipeBlueprint blueprint : methods)
                if(blueprint.methodName.matches(methodName)) return blueprint;
        return null;
    }

    private final String className;
    private final String methodName;
    private final String[] parameterTypes;

    public RecipeBlueprint(String className, String methodName, String ... parameterTypes) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        METHODS_BY_CLASS.putIfAbsent(className,new ArrayList<>());
        METHODS_BY_CLASS.get(className).add(this);
    }

    public String write(Object ... parameters) {
        StringBuilder builder = new StringBuilder(this.className+"."+this.methodName+"(");
        for(int i=0; i<parameters.length; i++) {
            builder.append(IOUtils.getWriterFunc(this.parameterTypes[i]).apply(parameters[i]));
            if(i+1<parameters.length) builder.append(",");
        }
        return builder.append(")").toString();
    }
}

package mods.thecomputerizer.scriptify.io.data;

import mods.thecomputerizer.scriptify.io.IOUtils;

import java.util.List;

public class RecipeBlueprint {

    private final String className;
    private final String methodName;
    private final String[] parameterTypes;

    public RecipeBlueprint(String className, String methodName, String ... parameterTypes) {
        this.className = className;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    public boolean matches(String simpleClass, String otherMethod) {
        if(this.methodName.matches(otherMethod)) {
            String[] splitClass = this.className.split("\\.");
            return simpleClass.matches(splitClass[splitClass.length-1]);
        }
        return false;
    }

    public boolean verifyArgs(List<Object> args) {
        for(int i=0; i<this.parameterTypes.length; i++) {

        }
        return true;
    }

    private boolean verifyArg(Object arg, String parameter) {

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

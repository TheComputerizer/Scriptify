package mods.thecomputerizer.scriptify.io.data;

import lombok.Getter;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.IOUtils;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.scriptify.util.Patterns;
import org.apache.commons.lang3.StringUtils;
import stanhebben.zenscript.type.ZenType;

import java.util.Arrays;
import java.util.Objects;


@Getter
public class DynamicArray {

    private final int bracketCount;
    private final String className;
    private final Class<?> typeClass;
    private final boolean isOptional;

    public DynamicArray(String unparsed) {
        this(StringUtils.countMatches(unparsed,'['),unparsed.replaceAll(Patterns.ARRAY_DEF.pattern(),""));
    }

    public DynamicArray(int bracketCount, String type) {
        this.bracketCount = bracketCount;
        this.isOptional = type.startsWith("@");
        if(this.isOptional) type = type.substring(1);
        if(bracketCount>0 && type.startsWith("L")) {
            type = type.substring(1);
            if(type.endsWith(";")) type = type.substring(0,type.length()-1);
        }
        this.className = IOUtils.getClassFromAlias(type).getName();
        this.typeClass = makeTypeClass();
    }

    public DynamicArray(int bracketCount, Class<?> clazz) {
        String name = clazz.getName();
        this.bracketCount = bracketCount<0 ? StringUtils.countMatches(name,'[') : bracketCount;
        name = name.replaceAll(Patterns.ARRAY_DEF.pattern(),"");
        this.isOptional = false;
        if(name.startsWith("L")) {
            name = name.substring(1);
            if(name.endsWith(";")) name = name.substring(0,name.length()-1);
        }
        this.className = name;
        this.typeClass = makeTypeClass();
    }

    public Class<?> getBaseClass() {
        Class<?> clazz;
        try {
            clazz = Class.forName(this.className);
        } catch(ClassNotFoundException ex) {
            Scriptify.logError(getClass(),null,ex);
            clazz = Object.class;
        }
        return clazz;
    }

    public boolean isValid(ZenType type) {
        if(Objects.isNull(type)) {
            Scriptify.logWarn(getClass(),"null",this.typeClass.getName());
            return false;
        }
        if(this.className.matches(Object.class.getName()))
            Scriptify.logWarn(getClass(),"type",this.typeClass.getName());
        Class<?> typeClass = IOUtils.getClassFromAlias(type.getName());
        return Blueprint.checkSpecialMatch(this.typeClass,typeClass) || this.typeClass.isAssignableFrom(typeClass);
    }

    private Class<?> makeTypeClass() {
        Class<?> clazz = getBaseClass();
        if(this.bracketCount==0) return clazz;
        int[] dimensions = new int[this.bracketCount];
        Arrays.fill(dimensions,1);
        Object ref = Misc.makeArray(clazz,dimensions);
        return ref.getClass();
    }

    @Override
    public boolean equals(Object o) {
        if(getClass()==o.getClass()) {
            DynamicArray d = (DynamicArray)o;
            return this.typeClass==d.typeClass && this.bracketCount==d.bracketCount;
        }
        return false;
    }

    @Override
    public String toString() {
        return (this.isOptional ? "@" : "")+this.typeClass.getName();
    }
}

package mods.thecomputerizer.scriptify.io.data;

import lombok.Getter;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.io.write.ArrayWriter;
import mods.thecomputerizer.scriptify.io.write.FileWriter;
import mods.thecomputerizer.scriptify.util.IOUtils;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.scriptify.util.Patterns;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;


@Getter
public class DynamicArray {

    private final int bracketCount;
    private final String className;
    private final Class<?> typeClass;

    public DynamicArray(String unparsed) {
        this(StringUtils.countMatches(unparsed,'['),unparsed.replaceAll(Patterns.ARRAY_DEF.pattern(),""));
    }

    public DynamicArray(int bracketCount, String type) {
        this.bracketCount = bracketCount;
        this.className = IOUtils.getClassFromAlias(type).getName();
        this.typeClass = makeTypeClass();
    }

    public boolean isValid(Object val) {
        if(Objects.isNull(val)) {
            Scriptify.logWarn(getClass(),"null",this.typeClass.getName());
            return false;
        }
        if(this.className.matches(Object.class.getName()))
            Scriptify.logWarn(getClass(),"type",this.typeClass.getName());
        return (Number.class.isAssignableFrom(this.typeClass) && Number.class.isAssignableFrom(val.getClass())) ||
                RecipeBlueprint.checkSpecialMatch(this.typeClass,val.getClass()) ||
                this.typeClass.isAssignableFrom(val.getClass());
    }

    private Class<?> makeTypeClass() {
        Class<?> clazz;
        try {
            clazz = Class.forName(this.className);
        } catch(ClassNotFoundException ex) {
            Scriptify.logError(getClass(),null,ex,this.typeClass.getName());
            clazz = Object.class;
        }
        if(this.bracketCount==0) return clazz;
        int[] dimensions = new int[this.bracketCount];
        Arrays.fill(dimensions,1);
        Object ref = Misc.makeArray(clazz,dimensions);
        return ref.getClass();
    }

    @SafeVarargs
    private final <A> ArrayWriter<A> makeArrayWriter(A... args) {
        ArrayWriter<A> writer = new ArrayWriter<>(0);
        MutableInt max = new MutableInt();
        writer.setElements(args,(clamped,arg) -> {
            FileWriter elementWriter = makeWriter(arg);
            max.setValue(elementWriter.getTabLevel());
            return elementWriter;
        });
        writer.setTabLevel(max.getAndAdd(1));
        writer.setNeedsSemicolon(false);
        return writer;
    }

    public <A> FileWriter makeWriter(A arg) {
        ScriptifyRef.LOGGER.error("Making writer for {}",arg);
        if(arg instanceof Object[]) return makeArrayWriter(arg);
        if(arg instanceof Collection<?>) return makeArrayWriter(((Collection<?>)arg).toArray(new Object[0]));
        return IOUtils.getWriter(arg.getClass().getSimpleName(),arg);
    }

    @Override
    public String toString() {
        return super.toString()+" with type "+getTypeClass().toString();
    }
}

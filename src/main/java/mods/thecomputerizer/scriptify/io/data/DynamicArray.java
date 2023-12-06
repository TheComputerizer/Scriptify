package mods.thecomputerizer.scriptify.io.data;

import lombok.Getter;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.IOUtils;
import mods.thecomputerizer.scriptify.io.write.ArrayWriter;
import mods.thecomputerizer.scriptify.io.write.IClampedStringWriter;
import mods.thecomputerizer.scriptify.io.write.SingletonWriter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


@Getter
public class DynamicArray {

    private final int bracketCount;
    private final String className;
    private final Class<?> typeClass;

    public DynamicArray(String unparsed) {
        this(StringUtils.countMatches(unparsed,'['),unparsed.replaceAll(IOUtils.ARRAY_TYPE_PATTERN.pattern(),""));
    }

    public DynamicArray(int bracketCount, String type) {
        this.bracketCount = bracketCount;
        this.className = IOUtils.getClassFromAlias(type).getName();
        this.typeClass = makeTypeClass();
    }

    /**
     * TODO This is implemented completely wrong and is for testing purposes only
     */
    public List<IClampedStringWriter> getWriters(Object val, int ... sizes) {
        List<IClampedStringWriter> writers = new ArrayList<>();
        Function<Object,String> toStringFunc = IOUtils.getWriterFunc(this.className);
        if(this.bracketCount==0) writers.add(new SingletonWriter(toStringFunc.apply(val)));
        for(int i=0; i<this.bracketCount; i++) {
            String[] test = new String[sizes.length>=i+1 ? sizes[i] : 1];
            for(int e=0; e<test.length; e++) test[e] = toStringFunc.apply(val);
            ArrayWriter<?> writer = new ArrayWriter<>(new SingletonWriter(test));
            writers.add(writer);
        }
        return writers;
    }

    public boolean isValid(Object val) {
        if(Objects.isNull(val)) {
            Scriptify.logWarn("Cannot check if null value can be assigned to type class {}! "+
                    "The type will be assumed invalid.",this.typeClass.getName());
            return false;
        }
        if(this.className.matches(Object.class.getName()))
            Scriptify.logWarn("Type class is set to {} which will be assumed to be valid but may break things!",
                    this.typeClass.getName());
        return (Number.class.isAssignableFrom(this.typeClass) && Number.class.isAssignableFrom(val.getClass())) ||
                RecipeBlueprint.checkSpecialMatch(this.typeClass,val.getClass()) ||
                this.typeClass.isAssignableFrom(val.getClass());
    }

    private Class<?> makeTypeClass() {
        Class<?> clazz;
        try {
            clazz = Class.forName(this.className);
        } catch(ClassNotFoundException ex) {
            Scriptify.logError("Unable to find class for name {} in DynamicArray object! Was the type stored "+
                    "correctly?",this.className,ex);
            clazz = Object.class;
        }
        if(this.bracketCount==0) return clazz;
        int[] dimensions = new int[this.bracketCount];
        Arrays.fill(dimensions,1);
        Object ref = IOUtils.makeArray(clazz,dimensions);
        return ref.getClass();
    }

    @Override
    public String toString() {
        return super.toString()+" with type "+getTypeClass().toString();
    }
}

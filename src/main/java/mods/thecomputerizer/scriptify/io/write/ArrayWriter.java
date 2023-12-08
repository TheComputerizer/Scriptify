package mods.thecomputerizer.scriptify.io.write;

import lombok.Setter;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ArrayWriter<E> extends ClampedWriter<E> {

    @Setter private String name;
    @Setter private boolean isVertical;
    private int level;

    public ArrayWriter(int tabLevel) {
        super(tabLevel);
        this.level = 1;
    }

    @Override
    protected String getStart(String first) {
        return (Objects.nonNull(this.name) ? this.name+" = " : "")+first;
    }

    private int incrementLevel(ArrayWriter<?> writer, int level) {
        int max = level;
        for(FileWriter element : writer.writers) {
            if(element instanceof ArrayWriter<?>) {
                writer.setVertical(true);
                max = Math.max(max,incrementLevel((ArrayWriter<?>)element,level)+1);
            }
        }
        return max;
    }

    protected boolean isNamed() {
        return Objects.nonNull(this.name);
    }

    @Override
    public void setElements(E[] elements, BiFunction<ClampedWriter<E>,E,FileWriter> writerFunc) {
        super.setElements(elements,writerFunc);
        this.level = incrementLevel(this,1);
    }

    @Override
    public String toString() {
        return this.writers.size()==0 ? StringUtils.repeat("[",this.level)+StringUtils.repeat("]",this.level) :
                getStart("[ ")+TextUtil.arrayToString(",",(Object[])this.writers.toArray())+getEnd("]");
    }

    @Override
    public void writeLines(List<String> lines) {
        if(this.writers.size()==0) tryAppend(lines,getStart(this.toString()),true);
        else {
            if(this.isVertical) {
                tryAppend(lines,getStart("[ "), true);
                writeBasic(lines);
                tryAppend(lines,getEnd("]"),true);
            } else {
                List<String> subLines = new ArrayList<>();
                tryAppend(subLines,getStart("[ "), true);
                writeBasic(subLines);
                tryAppend(subLines,getEnd("]"),true);
                lines.add(TextUtil.listToString(subLines,","));
            }
        }
    }
}

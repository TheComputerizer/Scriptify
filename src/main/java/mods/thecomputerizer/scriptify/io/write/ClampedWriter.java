package mods.thecomputerizer.scriptify.io.write;

import lombok.Setter;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.util.CollectionBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public abstract class ClampedWriter<E> extends FileWriter {

    protected final CollectionBundle<FileWriter> writers;
    @Setter private boolean needsSemicolon;

    public ClampedWriter(int tabLevel) {
        super(tabLevel);
        this.writers = CollectionBundle.make(ArrayList::new);
        this.needsSemicolon = true;
    }

    public void addElement(E element, BiFunction<ClampedWriter<E>,E,FileWriter> writerFunc) {
        this.writers.add(writerFunc.apply(this, element));
    }

    protected void applyWriters(List<String> lines) {

    }

    protected String getEnd(String end) {
        return end+(this.needsSemicolon ? "; " : "");
    }

    protected String getStart(String start) {
        return start+" ";
    }

    public void setElements(E[] elements, BiFunction<ClampedWriter<E>,E,FileWriter> writerFunc) {
        this.writers.clear();
        for(E element : elements) {
            addElement(element,writerFunc);
        }
    }

    protected void writeBasic(List<String> lines) {
        int index = 0;
        for(FileWriter writer : this.writers) {
            writer.setTabLevel(getTabLevel()+1);
            ScriptifyRef.LOGGER.error("Element class is {} and tab level is {}",writer.getClass().getName(),writer.getTabLevel());
            if(writer instanceof PartialWriter<?>)
                ScriptifyRef.LOGGER.error("Partial writer element is {}",((PartialWriter<?>)writer).getElement());
            writer.writeLines(lines);
            String append = index+1<this.writers.size() ? ", " : " ";
            tryAppend(lines,append,true);
            index++;
        }
    }
}

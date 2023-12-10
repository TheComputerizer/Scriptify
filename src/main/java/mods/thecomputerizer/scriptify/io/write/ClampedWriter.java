package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.scriptify.util.CollectionBundle;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Writer wrapper for delegating primitive and nonenclosing types to sub writers.
 * Sub writers will be enclosed or "clamped" by getStart and getEnd
 */
public class ClampedWriter extends FileWriter {

    @Getter protected final CollectionBundle<FileWriter> writers;
    @Setter private String prefix;
    @Setter private String strClose;
    @Setter private String strOpen;
    @Setter private String strSeparator;
    @Setter private boolean forceAppend;
    @Setter private boolean needsSemicolon;
    @Setter private boolean disableSpaces;

    public ClampedWriter(int tabLevel) {
        super(tabLevel);
        this.writers = CollectionBundle.make(ArrayList::new);
        this.needsSemicolon = true;
        this.strClose = ")";
        this.strOpen = "(";
        this.strSeparator = ", ";
    }

    public void addWriter(FileWriter writer) {
        this.writers.add(writer);
    }

    public <E> void addWriters(Function<E,FileWriter> toWriter, Iterable<E> things) {
        for(E thing : things) addWriter(toWriter.apply(thing));
    }

    public <E> void addWriters(Function<E,FileWriter> toWriter, E[] things) {
        for(E thing : things) addWriter(toWriter.apply(thing));
    }

    private String getStart() {
        return Misc.getNullable(this.prefix,this.prefix,"")+this.strOpen +
                Misc.getEither(this.disableSpaces,""," ");
    }

    private String getSeparator(boolean removeSpaces) {
        return Misc.getEither(removeSpaces,this.strSeparator.replaceAll(" ",""),this.strSeparator);
    }

    private String getEnd() {
        return this.strClose +(this.needsSemicolon ? ";" : "");
    }

    @Override
    public void setTabLevel(int numTabs) {
        this.tabLevel = numTabs;
        for(FileWriter writer : this.writers) writer.setTabLevel(numTabs+1);
    }

    @Override
    public void writeLines(List<String> lines) {
        if(this.forceAppend) {
            List<String> subLines = new ArrayList<>();
            writeSubs(subLines,true);
            write(lines,TextUtil.listToString(subLines,"").replaceAll("\t",""),true);
        } else writeSubs(lines,false);
    }

    private void writeSubs(List<String> lines, boolean removeSpaces) {
        write(lines,getStart(),true,false);
        int i = 0;
        for(FileWriter writer : this.writers) {
            if(writer instanceof ClampedWriter) writer.setNewLine(true);
            writer.writeLines(lines);
            i++;
            if(i<this.writers.size()) {
                boolean newLine = isNewLine();
                setNewLine(false);
                write(lines,getSeparator(removeSpaces),true,true);
                setNewLine(newLine);
            }
        }
        write(lines,getEnd(),true,true);
    }
}

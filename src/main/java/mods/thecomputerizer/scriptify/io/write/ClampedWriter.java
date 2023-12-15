package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.scriptify.util.iterator.Wrapperable;
import mods.thecomputerizer.scriptify.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Writer wrapper for delegating primitive and nonenclosing types to sub writers.
 * Sub writers will be enclosed or "clamped" by getStart and getEnd
 */
public class ClampedWriter extends FileWriter {

    @Getter protected final Wrapperable<FileWriter> writers;
    @Setter @Getter private String prefix;
    @Setter private String strClose;
    @Setter private String strOpen;
    @Setter private String strSeparator;
    @Setter private boolean needsSemicolon;
    @Setter private boolean disableSpaces;

    public ClampedWriter(int tabLevel) {
        super(tabLevel);
        this.writers = Wrapperable.make(ArrayList::new);
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

    @Override
    public void collectImports(Wrapperable<String> imports) {
        for(FileWriter writer : this.writers) writer.collectImports(imports);
    }

    @Override
    public void collectPreprocessors(Wrapperable<String> preprocessors) {
        for(FileWriter writer : this.writers) writer.collectPreprocessors(preprocessors);
    }

    private String getStart() {
        return Misc.getNullable(this.prefix,this.prefix,"")+this.strOpen +
                Misc.getEither(this.disableSpaces,""," ");
    }

    private String getSeparator() {
        return this.strSeparator;
    }

    private String getEnd(boolean removeSpaces) {
        String end = this.strClose +(this.needsSemicolon ? ";" : "");
        if(removeSpaces) end = end.replaceFirst(" ","");
        return end;
    }

    @Override
    protected Object getValueInner() {
        return this.writers.getAsList().stream().map(FileWriter::getValueInner).collect(Collectors.toList());
    }

    @Override
    public void setTabLevel(int numTabs) {
        this.tabLevel = numTabs;
        for(FileWriter writer : this.writers) writer.setTabLevel(numTabs+1);
    }

    @Override
    public void writeLines(List<String> lines) {
        boolean newLine = isNewLine();
        boolean elementNewLine = false;
        write(lines,getStart(),true,false);
        int i = 0;
        for(FileWriter writer : this.writers) {
            writer.writeLines(lines);
            if(writer.isNewLine()) elementNewLine = true;
            i++;
            if(i<this.writers.size()) {
                setNewLine(false);
                write(lines,getSeparator(),true,true);
                setNewLine(newLine);
            }
        }
        setNewLine(elementNewLine);
        write(lines,getEnd(elementNewLine),true,true);
        setNewLine(newLine);
    }
}

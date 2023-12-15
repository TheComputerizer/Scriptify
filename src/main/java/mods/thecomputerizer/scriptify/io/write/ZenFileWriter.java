package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import mods.thecomputerizer.scriptify.io.data.Blueprint;
import mods.thecomputerizer.scriptify.io.data.ExpressionData;
import mods.thecomputerizer.scriptify.util.iterator.Wrapperable;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ZenFileWriter extends FileWriter {

    private final Wrapperable<String> preProcessors;
    private final Wrapperable<String> imports;
    private final Wrapperable<FileWriter> writers;
    private boolean debug;

    public ZenFileWriter(FileWriter... entries) {
        this();
        this.writers.set(entries);
    }

    public ZenFileWriter(Collection<FileWriter> entries) {
        this();
        this.writers.set(entries);
    }

    private ZenFileWriter() {
        super(0);
        this.preProcessors = Wrapperable.make(HashSet::new);
        this.imports = Wrapperable.make(HashSet::new);
        this.writers = Wrapperable.make(HashSet::new);
    }

    public void addPreProcessor(String processor) {
        this.preProcessors.add(processor);
    }

    public void finalizeData(ExpressionData data) {
        Blueprint blueprint = data.getBlueprint();
        if(blueprint.isReloadable()) this.preProcessors.add("reloadable");
        if(!blueprint.isGlobal()) this.imports.add(blueprint.getClassName());
    }

    @Override
    public Object getValueInner() {
        return this.writers.stream().map(FileWriter::getValueInner).collect(Collectors.toSet());
    }

    @Override
    public void writeLines(List<String> lines) {
        List<String> writerLines = new ArrayList<>();
        for(FileWriter writer : this.writers) {
            writer.writeLines(writerLines);
            addBlankLine(writerLines);
            writer.collectImports(this.imports);
            writer.collectPreprocessors(this.preProcessors);
        }
        writeComments(lines);
        if(this.preProcessors.isNotEmpty()) {
            for(String processor : this.preProcessors) lines.add("#"+processor);
            addBlankLine(lines);
        }
        if(this.imports.isNotEmpty()) {
            for(String className : this.imports) lines.add("import "+className+";");
            addBlankLine(lines);
        }
        lines.addAll(writerLines);
    }

    public void writeToFile(String filePath, boolean overwrite) {
        List<String> lines = new ArrayList<>();
        writeLines(lines);
        FileUtil.writeLinesToFile(filePath,lines,!overwrite);
    }
}

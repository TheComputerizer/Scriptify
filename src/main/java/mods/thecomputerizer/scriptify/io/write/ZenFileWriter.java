package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;

import java.util.*;
import java.util.stream.Collectors;

public class ZenFileWriter extends FileWriter {

    private Set<String> preProcessors;
    @Getter private Set<String> imports;
    @Getter private final Set<FileWriter> writers;
    @Setter private boolean debug;

    public ZenFileWriter(FileWriter... entries) {
        super(0);
        this.preProcessors = new HashSet<>();
        this.imports = new HashSet<>();
        this.writers = new HashSet<>();
        this.writers.addAll(Arrays.asList(entries));
    }

    public ZenFileWriter(Collection<FileWriter> entries) {
        super(0);
        this.preProcessors = new HashSet<>();
        this.imports = new HashSet<>();
        this.writers = new HashSet<>(entries);
    }

    public void addImport(String className) {
        this.imports.add(className);
    }

    public void addPreProcessor(String processor) {
        this.preProcessors.add(processor);
    }

    @Override
    public Object getValue() {
        return this.writers.stream().map(FileWriter::getValue).collect(Collectors.toSet());
    }

    public void setImports(String ... classNames)  {
        this.imports = new HashSet<>(Arrays.asList(classNames));
    }

    public void setPreProcessors(String ... processors) {
        this.preProcessors = new HashSet<>(Arrays.asList(processors));
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
        if(!this.preProcessors.isEmpty()) {
            for(String processor : this.preProcessors) lines.add("#"+processor);
            addBlankLine(lines);
        }
        if(!this.imports.isEmpty()) {
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

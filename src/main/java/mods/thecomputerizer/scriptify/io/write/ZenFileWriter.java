package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;

import java.util.*;

public class ZenFileWriter extends FileWriter {

    private List<String> preProcessors;
    private List<String> imports;
    @Getter private final Set<FileWriter> writers;
    @Setter private boolean debug;

    public ZenFileWriter(FileWriter... entries) {
        super(0);
        this.preProcessors = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.writers = new HashSet<>();
        this.writers.addAll(Arrays.asList(entries));
    }

    public ZenFileWriter(Collection<FileWriter> entries) {
        super(0);
        this.preProcessors = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.writers = new HashSet<>(entries);
    }

    public void addImport(String className) {
        this.imports.add(className);
    }

    public void addPreProcessor(String processor) {
        this.preProcessors.add(processor);
    }

    public void setImports(String ... classNames)  {
        this.imports = Arrays.asList(classNames);
    }

    public void setPreProcessors(String ... processors) {
        this.preProcessors = Arrays.asList(processors);
    }

    @Override
    public void writeLines(List<String> lines) {
        writeComments(lines);
        if(!this.preProcessors.isEmpty()) {
            for(String processor : this.preProcessors) lines.add("#"+processor);
            lines.add("");
        }
        if(!this.imports.isEmpty()) {
            for(String className : this.imports) lines.add("import "+className+";");
            lines.add("");
        }
        for(FileWriter writer : this.writers) {
            writer.writeLines(lines);
            lines.add("");
        }
    }

    public void writeToFile(String filePath, boolean overwrite) {
        List<String> lines = new ArrayList<>();
        writeLines(lines);
        FileUtil.writeLinesToFile(filePath,lines,!overwrite);
    }
}

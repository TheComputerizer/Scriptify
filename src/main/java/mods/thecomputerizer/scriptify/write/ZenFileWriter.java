package mods.thecomputerizer.scriptify.write;

import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;

import java.util.*;
import java.util.stream.Collectors;

public class ZenFileWriter extends CommentedWriter<ZenFileWriter> {

    private List<String> preProcessors;
    private List<String> imports;
    private final Set<IStringify<?>> entries;

    public ZenFileWriter(IStringify<?> ... entries) {
        this.preProcessors = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.entries = new HashSet<>();
        for(IStringify<?> entry : entries) this.add(entry);
    }

    public ZenFileWriter(Collection<IStringify<?>> entries) {
        this.preProcessors = new ArrayList<>();
        this.imports = new ArrayList<>();
        this.entries = new HashSet<>(entries);
    }

    @Override
    public void add(IStringify<?> other) {
        this.entries.add(other);
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
    public List<String> stringify() {
        List<String> entryStrings = new ArrayList<>();
        List<String> comments = getComments();
        if(!comments.isEmpty()) {
            for(String comment : comments) entryStrings.add("//" + comment);
            entryStrings.add("");
        }
        if(!this.preProcessors.isEmpty()) {
            for(String processor : this.preProcessors) entryStrings.add("#"+processor);
            entryStrings.add("");
        }
        if(!this.imports.isEmpty()) {
            for(String className : this.imports) entryStrings.add("import "+className+";");
            entryStrings.add("");
        }
        for(IStringify<?> entry : this.entries) {
            for(String comment : entry.getComments()) entryStrings.add("//"+comment);
            entryStrings.add(entry.toString());
            entryStrings.add("");
        }
        return entryStrings;
    }

    @Override
    public void write(String filePath, boolean overwrite) {
        FileUtil.writeLinesToFile(filePath,stringify(),!overwrite);
    }
}

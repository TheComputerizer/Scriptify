package mods.thecomputerizer.scriptify.io.write;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionWriter extends CommentedWriter {

    protected List<SingletonWriter> writers;

    public CollectionWriter() {
        this.writers = new ArrayList<>();
    }

    @Override
    public List<String> getClampedLines() {
        return Collections.emptyList();
    }
}

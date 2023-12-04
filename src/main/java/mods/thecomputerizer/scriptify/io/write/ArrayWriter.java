package mods.thecomputerizer.scriptify.io.write;

import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;

import java.util.Collections;
import java.util.List;

public class ArrayWriter<A extends IClampedStringWriter> extends CommentedWriter {

    private final A[] entries;

    @SafeVarargs
    public ArrayWriter(A ... entries) {
        this.entries = entries;
    }

    @Override
    public List<String> getClampedLines() {
        return Collections.singletonList(this.toString());
    }

    @Override
    public String toString() {
        return "["+TextUtil.arrayToString(",",(Object[])this.entries)+"]";
    }
}

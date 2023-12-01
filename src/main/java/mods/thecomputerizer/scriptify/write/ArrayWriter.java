package mods.thecomputerizer.scriptify.write;

import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;

import java.util.Collections;
import java.util.List;

public class ArrayWriter<A extends IStringify<A>> extends UncommentedWriter<ArrayWriter<A>> {

    private final A[] entries;

    @SafeVarargs
    public ArrayWriter(A ... entries) {
        this.entries = entries;
    }

    @Override
    public void add(IStringify<?> other) {}

    @Override
    public List<String> stringify() {
        return Collections.singletonList(this.toString());
    }

    @Override
    public String toString() {
        return "["+TextUtil.arrayToString(",",(Object[])this.entries)+"]";
    }

    @Override
    public void write(String filePath, boolean overwrite) {}
}

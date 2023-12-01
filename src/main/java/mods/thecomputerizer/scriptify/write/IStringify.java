package mods.thecomputerizer.scriptify.write;

import java.util.List;

public interface IStringify<T> {

    void add(IStringify<?> other);
    List<String> getComments();
    List<String> stringify();
    void write(String filePath, boolean overwrite);
}

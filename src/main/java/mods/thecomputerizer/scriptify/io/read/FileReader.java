package mods.thecomputerizer.scriptify.io.read;

import java.util.List;

public interface FileReader<E> {

    void copy(List<String> lines);
    E parse(String unparsed);
}

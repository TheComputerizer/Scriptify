package mods.thecomputerizer.scriptify.write;

import java.util.Collections;
import java.util.List;

public abstract class UncommentedWriter<U extends UncommentedWriter<U>> implements IStringify<U> {

    @Override
    public List<String> getComments() {
        return Collections.emptyList();
    }
}

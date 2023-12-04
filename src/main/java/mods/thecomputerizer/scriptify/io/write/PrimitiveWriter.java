package mods.thecomputerizer.scriptify.io.write;

import java.util.Collections;
import java.util.List;

public class PrimitiveWriter extends CommentedWriter {

    @Override
    public List<String> getClampedLines() {
        return Collections.singletonList(this.toString());
    }
}

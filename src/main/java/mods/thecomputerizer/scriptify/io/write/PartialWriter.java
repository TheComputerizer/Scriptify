package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.io.data.BEP;

import java.util.List;
import java.util.Objects;

/**
 * Handles writing things that may take up less than a full line
 */
@Getter
@Setter
public class PartialWriter<E> extends FileWriter {

    private E element;

    public PartialWriter() {
        this(0);
    }

    public PartialWriter(int tabLevel) {
        super(tabLevel);
    }

    public void logTypeClass() {
        if(Objects.isNull(this.element)) ScriptifyRef.LOGGER.error("Element is null");
        else ScriptifyRef.LOGGER.error("Element is class {}",element.getClass().getName());
    }

    @Override
    public void writeLines(List<String> lines) {
        tryAppend(lines,toString(),false);
    }

    @Override
    public String toString() {
        if(Objects.nonNull(this.element)) {
            BEP bep = BEP.of(this.element);
            if(Objects.nonNull(bep)) return bep.toString();
        }
        return Objects.nonNull(this.element) ? this.element.toString() : "null";
    }
}

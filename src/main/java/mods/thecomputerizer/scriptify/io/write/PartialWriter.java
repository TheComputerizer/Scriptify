package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * Handles writing things that may take up less than a full line
 */
@Getter
@Setter
public class PartialWriter<E> extends FileWriter {

    protected E element;

    public PartialWriter() {
        this(0);
    }

    public PartialWriter(int tabLevel) {
        super(tabLevel);
    }

    @Override
    public Object getValue() {
        return getElement();
    }

    @Override
    public String toString() {
        return Objects.nonNull(this.element) ? this.element.toString() : "null";
    }

    @Override
    public void writeLines(List<String> lines) {
        write(lines,toString(),true);
    }
}

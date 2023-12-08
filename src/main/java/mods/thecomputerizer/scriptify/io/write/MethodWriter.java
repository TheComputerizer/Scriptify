package mods.thecomputerizer.scriptify.io.write;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Setter
public class MethodWriter extends ClampedWriter<FileWriter> {

    private String className;
    private String methodName;

    public MethodWriter(int tabLevel) {
        super(tabLevel);
    }

    public void addWriter(FileWriter writer) {
        addElement(writer,(clamped,element) -> writer);
    }

    @Override
    protected String getStart(String start) {
        return (StringUtils.isNotBlank(this.className) ? this.className+"." : "")+
                (StringUtils.isNotBlank(this.methodName) ? this.methodName : "")+start;
    }

    @Override
    public void writeLines(List<String> lines) {
        writeComments(lines);
        lines.add("");
        tryAppend(lines,getStart("("),false);
        writeBasic(lines);
        tryAppend(lines,getEnd(")"),true);
    }
}

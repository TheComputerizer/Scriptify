package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.scriptify.util.CollectionBundle;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Writes lines to a file based a list of strings that gets passed through.
 * Individual writers can be commented
 */
@Getter
public abstract class FileWriter {

    protected final CollectionBundle<String> comments;
    @Setter private int characterLimit;
    @Setter private boolean newLine;
    @Setter protected int tabLevel; //Number of tabs if this writer need to write a new line

    protected FileWriter(int tabLevel) {
        this.comments = CollectionBundle.make(ArrayList::new);
        this.characterLimit = 80;
        this.tabLevel = tabLevel;
    }

    public void addBlankLine(List<String> lines) {
        lines.add("");
    }

    public FileWriter incrementTabLevel() {
        this.tabLevel++;
        return this;
    }

    @Override
    public String toString() {
        try {
            List<String> lines = new ArrayList<>();
            writeLines(lines);
            String str = TextUtil.listToString(lines, " ");
            return Misc.getNullable(str, "null", str.trim());
        } catch (NullPointerException ex) {
            return "null";
        }
    }

    protected void tryAppend(List<String> lines, String str, boolean ignoreCharacterLimit) {
        if(!this.newLine && !lines.isEmpty()) {
            String last = lines.get(lines.size()-1);
            if(ignoreCharacterLimit || last.length()+str.length()<=this.characterLimit) {
                lines.remove(lines.size()-1);
                last+=str;
                lines.add(last);
            } else write(lines,str);
        } else write(lines,str);
    }

    protected String withTabs(String str) {
        return StringUtils.repeat("\t",this.tabLevel)+str;
    }

    protected void writeComments(List<String> lines) {
        int size = this.comments.size();
        if(size==0) return;
        if(size==1) lines.add(this.comments.get(0));
        else {
            lines.add("/*");
            for(String comment : this.comments) lines.add("\t"+comment);
            lines.add("*/");
        }
    }

    public abstract void writeLines(List<String> lines);

    protected void write(List<String> lines, String str) {
        write(lines,str,false,false);
    }
    protected void write(List<String> lines, String str, boolean tryAppend) {
        write(lines,str,tryAppend,false);
    }

    /**
     * Tries to append the string to the previous line.
     * Writes the string to a new line with the correct number of tabs if that fails or if tryAppend is false
     */
    protected void write(List<String> lines, String str, boolean tryAppend, boolean ignoreCharacterLimit) {
        if(tryAppend) tryAppend(lines,str,ignoreCharacterLimit);
        else lines.add(withTabs(str));
    }
}

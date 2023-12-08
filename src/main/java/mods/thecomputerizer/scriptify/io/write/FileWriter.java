package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.scriptify.util.CollectionBundle;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes lines to a file based a list of strings that gets passed through.
 * Individual writers can be commented
 */
public abstract class FileWriter {

    @Getter protected final CollectionBundle<String> comments;
    @Setter @Getter private int characterLimit;
    @Setter private boolean newLine;
    @Setter @Getter private int tabLevel;

    protected FileWriter(int tabLevel) {
        this.comments = CollectionBundle.make(ArrayList::new);
        this.characterLimit = 80;
        this.tabLevel = tabLevel;
    }

    public void incrementTabLevel() {
        this.tabLevel++;
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

    protected void tryAppend(List<String> lines, String str, boolean ignoreCharacterLimit) {
        if(!this.newLine && !lines.isEmpty()) {
            String last = lines.get(lines.size()-1);
            if(ignoreCharacterLimit || last.length()+str.length()<=this.characterLimit) {
                lines.remove(lines.size()-1);
                last+=str;
                lines.add(last);
            } else lines.add(withTabs(str));
        } else lines.add(withTabs(str));
    }

    protected String withTabs(String str) {
        return StringUtils.repeat("\t",this.tabLevel)+str;
    }

    public abstract void writeLines(List<String> lines);
}

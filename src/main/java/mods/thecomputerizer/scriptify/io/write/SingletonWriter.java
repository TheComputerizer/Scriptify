package mods.thecomputerizer.scriptify.io.write;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SingletonWriter extends CommentedWriter {

    protected final Object obj;
    protected List<String> comments;

    public SingletonWriter(Object obj) {
        this.obj = obj;
        this.comments = new ArrayList<>();
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    @Override
    public List<String> getComments() {
        return this.comments;
    }

    @Override
    public List<String> getClampedLines() {
        List<String> lines = new ArrayList<>();
        if(!this.comments.isEmpty()) {
            for(String comment : this.comments) lines.add("//"+comment);
            lines.add("");
        }
        lines.add(this.obj.toString());
        return lines;
    }

    public void setComments(String ... comments) {
        this.comments = Arrays.asList(comments);
    }
}

package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public abstract class CommentedWriter implements IClampedStringWriter {

    private List<String> comments = new ArrayList<>();

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    public void setComments(String ... comments) {
        this.comments = Arrays.asList(comments);
    }
}

package mods.thecomputerizer.scriptify.write;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CommentedWriter<C extends CommentedWriter<C>> implements IStringify<C> {

    private List<String> comments = new ArrayList<>();

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    @Override
    public List<String> getComments() {
        return this.comments;
    }

    public void setComments(String ... comments) {
        this.comments = Arrays.asList(comments);
    }
}

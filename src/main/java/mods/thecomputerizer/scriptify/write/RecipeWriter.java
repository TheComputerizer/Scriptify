package mods.thecomputerizer.scriptify.write;

import mods.thecomputerizer.scriptify.data.RecipeData;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeWriter extends CommentedWriter<RecipeWriter> {

    private final RecipeData[] data;
    public RecipeWriter(RecipeData ... data) {
        this.data = data;
    }

    @Override
    public void add(IStringify<?> other) {

    }

    @Override
    public List<String> stringify() {
        return Stream.of(this.data).map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return TextUtil.listToString(this.stringify())+";";
    }

    @Override
    public void write(String filePath, boolean overwrite) {}
}

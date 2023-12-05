package mods.thecomputerizer.scriptify.io.data;

import lombok.Getter;

import java.util.function.Function;

public class DynamicArray<T> {

    @Getter private final int levels;
    private final Function<String,T> parser;

    public DynamicArray(String brackets, Function<String,T> parser) {
        this(brackets.split("\\[").length-1,parser);
    }

    public DynamicArray(int levels, Function<String,T> parser) {
        this.levels = levels;
        this.parser = parser;
    }
}

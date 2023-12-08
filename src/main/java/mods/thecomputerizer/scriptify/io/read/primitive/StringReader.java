package mods.thecomputerizer.scriptify.io.read.primitive;

import mods.thecomputerizer.scriptify.io.read.FileReader;

import java.util.List;

public class StringReader implements FileReader<String> {

    @Override
    public void copy(List<String> lines) {

    }

    @Override
    public String parse(String unparsed) {
        return unparsed;
    }
}

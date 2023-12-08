package mods.thecomputerizer.scriptify.io.read.primitive;

import mods.thecomputerizer.scriptify.io.read.FileReader;

import java.util.List;

public class IntReader implements FileReader<Integer> {

    @Override
    public void copy(List<String> lines) {

    }

    @Override
    public Integer parse(String unparsed) {
        return Integer.parseInt(unparsed);
    }
}

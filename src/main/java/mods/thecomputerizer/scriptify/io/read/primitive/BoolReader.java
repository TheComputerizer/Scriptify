package mods.thecomputerizer.scriptify.io.read.primitive;

import mods.thecomputerizer.scriptify.io.read.FileReader;

import java.util.List;

public class BoolReader implements FileReader<Boolean> {

    @Override
    public void copy(List<String> lines) {

    }

    @Override
    public Boolean parse(String unparsed) {
        return Boolean.parseBoolean(unparsed);
    }
}

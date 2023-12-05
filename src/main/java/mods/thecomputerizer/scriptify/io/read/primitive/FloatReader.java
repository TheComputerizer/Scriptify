package mods.thecomputerizer.scriptify.io.read.primitive;

import mods.thecomputerizer.scriptify.io.read.IClampedStringReader;

import java.util.List;

public class FloatReader implements IClampedStringReader<Float> {

    @Override
    public void copy(List<String> lines) {

    }

    @Override
    public Float parse(String unparsed) {
        return Float.parseFloat(unparsed);
    }
}

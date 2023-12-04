package mods.thecomputerizer.scriptify.io.read;

import mods.thecomputerizer.scriptify.io.IOUtils;

import java.util.List;
import java.util.function.Function;

public interface IClampedStringReader<E> {

    void copy(List<String> lines);
    E parse(String unparsed);

    enum ReaderType {

        BEP(IOUtils::parseBEP),
        PRIMITIVE(Object::toString);

        private final Function<String,Object> fromStringFunc;
        ReaderType(Function<String,Object> fromStringFunc) {
            this.fromStringFunc = fromStringFunc;
        }

        public Object read(String unparsed) {
            return fromStringFunc.apply(unparsed);
        }
    }
}

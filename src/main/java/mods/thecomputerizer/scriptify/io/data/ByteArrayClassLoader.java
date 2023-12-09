package mods.thecomputerizer.scriptify.io.data;

import java.util.HashMap;
import java.util.Map;

public class ByteArrayClassLoader extends ClassLoader {

    private static final Map<String,Byte[]> BYTE_MAP = new HashMap<>();
    public static void addClassByte(String name, byte[] bytes) {
        Byte[] boxed = new Byte[bytes.length];
        for(int i=0; i<bytes.length; i++) boxed[i] = bytes[i];
        BYTE_MAP.put(name.replace('/','.'),boxed);
    }

    public Class<?> findClass(String name) {
        name = name.replace('/','.');
        Byte[] boxed = BYTE_MAP.getOrDefault(name,new Byte[0]);
        byte[] bytes = new byte[boxed.length];
        for(int i=0; i<boxed.length; i++) bytes[i] = boxed[i];
        return defineClass(name,bytes,0,bytes.length);
    }
}

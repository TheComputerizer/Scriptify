package mods.thecomputerizer.scriptify;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScriptifyRef {

    public static final String MODID = "scriptify";
    public static final String NAME = "Scriptify";
    public static final String VERSION = "0.0.1";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2860,);"+
            "required-after:theimpossiblelibrary;";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
}

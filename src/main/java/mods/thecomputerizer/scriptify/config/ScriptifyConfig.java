package mods.thecomputerizer.scriptify.config;

import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.command.ISubType;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import static net.minecraftforge.common.config.Config.*;

@EventBusSubscriber(modid = ScriptifyRef.MODID)
@Config(modid = ScriptifyRef.MODID, name = ScriptifyRef.NAME, category = "")
public class ScriptifyConfig {

    @Name("commands")
    @LangKey("config."+ScriptifyRef.MODID+".commands")
    public static Commands COMMANDS = new Commands();


    public static class Commands {

        @Name("defaultParameterValues")
        @Comment("The default parameter values for parameters not directly specified in commands")
        @LangKey("config."+ScriptifyRef.MODID+".commands.defaultParameterValues")
        public String[] defaultParameterValues = ISubType.Type.getDefaultsParameters();
    }
}

package mods.thecomputerizer.scriptify.config;

import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.command.ISubType;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.common.config.Config.*;

@EventBusSubscriber(modid = ScriptifyRef.MODID)
@Config(modid = ScriptifyRef.MODID, name = ScriptifyRef.NAME, category = "")
public class ScriptifyConfig {

    @Name("commands")
    @LangKey("config."+ScriptifyRef.MODID+".commands")
    public static Commands COMMANDS = new Commands();

    @Name("misc")
    @LangKey("config."+ScriptifyRef.MODID+".misc")
    public static Misc MISC = new Misc();

    @Name("parameters")
    @LangKey("config."+ScriptifyRef.MODID+".parameters")
    public static Parameters PARAMETERS = new Parameters();

    public static class Commands {

        @Name("commandBuilders")
        @Comment("Determines the file from which prebuilt commands will be read from")
        @LangKey("config."+ScriptifyRef.MODID+".commands.commandBuilders")
        public String commandBuilders = "commands.txt";
    }

    public static class Misc {

        @Name("logLangFolder")
        @Comment("Determines the folder from which lang files for translated log messages will be read from")
        @LangKey("config."+ScriptifyRef.MODID+".misc.logLangFolder")
        public String logLangFolder = "lang";

        @Name("logLangDefault")
        @Comment("Determines the default locale for translated log messages before it gets synced from the client")
        @LangKey("config."+ScriptifyRef.MODID+".misc.logLangDefault")
        public String logLangDefault = "en_us";
    }

    public static class Parameters {

        @Name("defaultParameterValues")
        @Comment("The default parameter values for parameters not directly specified in commands")
        @LangKey("config."+ScriptifyRef.MODID+".parameters.defaultParameterValues")
        public String[] defaultParameterValues = ISubType.Type.getDefaultsParameters();

        @Name("parameters")
        @Comment("Determines the file from which custom default parameter sets will be read from")
        @LangKey("config."+ScriptifyRef.MODID+".parameters.parameters")
        public String parameters = "parameters.txt";
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(ScriptifyRef.MODID)) {
            ConfigManager.sync(event.getModID(),Config.Type.INSTANCE);
            ScriptifyConfigHelper.onConfigReloaded();
        }
    }
}

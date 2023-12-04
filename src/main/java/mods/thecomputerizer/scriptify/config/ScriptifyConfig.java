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

    @Name("parameters")
    @LangKey("config."+ScriptifyRef.MODID+".parameters")
    public static Parameters PARAMETERS = new Parameters();

    public static class Commands {}

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

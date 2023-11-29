package mods.thecomputerizer.scriptify.registry;

import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.command.ScriptifyCommand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod.EventBusSubscriber(modid = ScriptifyRef.MODID)
public class RegistryHandler {

    public static void onServerStarting(FMLServerStartingEvent event) {
        ScriptifyRef.LOGGER.info("Registering command tree");
        event.registerServerCommand(new ScriptifyCommand());
    }
}

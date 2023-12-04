package mods.thecomputerizer.scriptify;

import mods.thecomputerizer.scriptify.command.ScriptifyCommands;
import mods.thecomputerizer.scriptify.io.IOUtils;
import mods.thecomputerizer.scriptify.network.PacketQueryContainer;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import mods.thecomputerizer.theimpossiblelibrary.network.NetworkHandler;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;
import java.util.Objects;

@Mod(modid = ScriptifyRef.MODID, name = ScriptifyRef.NAME, version = ScriptifyRef.VERSION,
        dependencies = ScriptifyRef.DEPENDENCIES)
public class Scriptify {

    public Scriptify() {
        IOUtils.loadDefaults();
        NetworkHandler.queueClientPacketRegister(PacketQueryContainer.class);
        NetworkHandler.queueServerPacketRegister(PacketSendContainerInfo.class);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {}

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {}

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ScriptifyCommands());
    }

    public static File getConfigFile(String path) {
        path = "config/"+ScriptifyRef.NAME+"/"+path;
        return FileUtil.generateNestedFile(new File(path),false);
    }

    public static ResourceLocation getResource(String path) {
        return new ResourceLocation(ScriptifyRef.MODID,path);
    }

    public static String langKey(String type, String ... args) throws IllegalArgumentException {
        if(Objects.isNull(args) || args.length<1)
            throw new IllegalArgumentException("Lang keys with no arguments are not supported!");
        StringBuilder keyBuilder = new StringBuilder(type+"."+ScriptifyRef.MODID+".");
        for(int i=0; i<args.length; i++) {
            keyBuilder.append(args[i]);
            if((i+1)<args.length) keyBuilder.append(".");
        }
        return keyBuilder.toString();
    }

    public static void logDebug(String msg, Object ... args) {
        ScriptifyRef.LOGGER.debug(msg,args);
    }

    public static void logError(String msg, Object ... args) {
        ScriptifyRef.LOGGER.error(msg,args);
    }

    public static void logFatal(String msg, Object ... args) {
        ScriptifyRef.LOGGER.error(msg,args);
    }

    public static void logInfo(String msg, Object ... args) {
        ScriptifyRef.LOGGER.info(msg,args);
    }

    public static void logWarn(String msg, Object ... args) {
        ScriptifyRef.LOGGER.warn(msg,args);
    }
}

package mods.thecomputerizer.scriptify;

import mods.thecomputerizer.scriptify.command.ScriptifyCommands;
import mods.thecomputerizer.scriptify.network.PacketQueryContainer;
import mods.thecomputerizer.scriptify.network.PacketSendContainerInfo;
import mods.thecomputerizer.scriptify.util.IOUtils;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.theimpossiblelibrary.network.NetworkHandler;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
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

    public static ITextComponent getText(String textStr, boolean isTranslation, Object ... args) {
        return isTranslation ? new TextComponentTranslation(textStr,args) : new TextComponentString(textStr);
    }

    public static String getTranslated(String langKey, Object ... args) {
        return getText(langKey,true,args).getFormattedText();
    }

    public static void log(Level level, String msg, Object ... args) {
        Logger log = ScriptifyRef.LOGGER;
        if(level==Level.DEBUG) log.debug(msg,args);
        else if(level==Level.INFO) log.info(msg,args);
        else if(level==Level.WARN) log.warn(msg,args);
        else if(level==Level.ERROR) log.error(msg,args);
        else if(level==Level.FATAL) log.fatal(msg,args);
    }

    public static void logDebug(Class<?> clazz) {
        logDebug(clazz,null);
    }

    public static void logDebug(Class<?> clazz, @Nullable String qualifier, Object ... args) {
        ScriptifyRef.LOGGER.debug(Misc.translateLog(clazz,"debug",qualifier,args));
    }

    public static void logError(Class<?> clazz) {
        logError(clazz,null);
    }

    public static void logError(Class<?> clazz, Throwable ex) {
        logError(clazz,null,ex);
    }

    public static void logError(Class<?> clazz, @Nullable String qualifier, @Nullable Throwable ex, Object ... args) {
        Logger log = ScriptifyRef.LOGGER;
        String level = Objects.nonNull(ex) ? "exception" : "error";
        if(Objects.nonNull(ex)) log.error(Misc.translateLog(clazz,level,qualifier,args),ex);
        else log.error(Misc.translateLog(clazz,level,qualifier,args));
    }

    public static void logFatal(Class<?> clazz) {
        logFatal(clazz,null);
    }

    public static void logFatal(Class<?> clazz, Throwable ex) {
        logFatal(clazz,null,ex);
    }

    public static void logFatal(Class<?> clazz, @Nullable String qualifier, @Nullable Throwable ex, Object ... args) {
        Logger log = ScriptifyRef.LOGGER;
        String level = Objects.nonNull(ex) ? "exception" : "fatal";
        if(Objects.nonNull(ex)) log.fatal(Misc.translateLog(clazz,level,qualifier,args),ex);
        else log.fatal(Misc.translateLog(clazz,level,qualifier,args));
    }

    public static void logInfo(Class<?> clazz) {
        logInfo(clazz,null);
    }

    public static void logInfo(Class<?> clazz, @Nullable String qualifier, Object ... args) {
        ScriptifyRef.LOGGER.info(Misc.translateLog(clazz,"info",qualifier,args));
    }

    public static void logWarn(Class<?> clazz) {
        logWarn(clazz,null);
    }

    public static void logWarn(Class<?> clazz, @Nullable String qualifier, Object ... args) {
        ScriptifyRef.LOGGER.warn(Misc.translateLog(clazz,"warn",qualifier,args));
    }

    public static String makeLangKey(String type, String ... args) throws IllegalArgumentException {
        if(Objects.isNull(args) || args.length<1)
            throw new IllegalArgumentException(Misc.translateLog(Scriptify.class,"exception",null));
        StringBuilder keyBuilder = new StringBuilder(String.format("%s.%s.",type,ScriptifyRef.MODID));
        for(int i=0; i<args.length; i++) {
            keyBuilder.append(args[i]);
            if((i+1)<args.length) keyBuilder.append(".");
        }
        return keyBuilder.toString();
    }
}

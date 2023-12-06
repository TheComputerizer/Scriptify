package mods.thecomputerizer.scriptify.command.parameters;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Parser {

    public static boolean parseBool(String val) {
        return Boolean.parseBoolean(val.trim().toLowerCase());
    }

    public static double parseDouble(Parameter<?> parameter, String val) throws CommandException {
        return parseDouble(parameter,val,0d);
    }

    public static double parseDouble(Parameter<?> parameter, String val, double base) throws CommandException {
        try {
            return val.contains("~") ? base+Double.parseDouble(val.trim().replaceAll("~","")) :
                    Double.parseDouble(val.trim());
        } catch(NumberFormatException ex) {
            parameter.throwGeneric("number",parameter.getName(),val);
            return 0d;
        }
    }

    public static int parseInt(Parameter<?> parameter, String val) throws CommandException {
        return parseInt(parameter,val,0);
    }

    public static int parseInt(Parameter<?> parameter, String val, int base) throws CommandException {
        try {
            return val.contains("~") ? base+Integer.parseInt(val.trim().replaceAll("~","")) :
                    Integer.parseInt(val.trim());
        } catch(NumberFormatException ex) {
            parameter.throwGeneric("number",parameter.getName(),val);
            return 0;
        }
    }

    public static <T> List<T> parseArray(MinecraftServer server, ICommandSender sender, Parameter<T> parameter,
                                         String val) throws CommandException {
        List<T> list = new ArrayList<>();
        if(!val.startsWith("[") || !val.endsWith("]")) {
            parameter.setValueStr(val);
            addToList(list,parameter.execute(server,sender));
        }
        else {
            String trimmed = val.trim().substring(1);
            trimmed = trimmed.substring(0, trimmed.length() - 1);
            String[] split = trimmed.split(",");
            for(String element : split) {
                parameter.setValueStr(element);
                addToList(list,parameter.execute(server, sender));
            }
        }
        return list;
    }

    private static <T> void addToList(List<T> list, @Nullable T val) {
        String str = Objects.nonNull(val) ? val.toString() : null;
        if(StringUtils.isNotBlank(str)) list.add(val);
    }
}

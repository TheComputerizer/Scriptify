package mods.thecomputerizer.scriptify.command.parameters;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        if(!val.startsWith("[") || !val.endsWith("]")) {
            parameter.setValueStr(val);
            return Collections.singletonList(parameter.execute(server,sender));
        }
        String trimmed = val.trim().substring(1);
        trimmed = trimmed.substring(0,trimmed.length()-1);
        String[] split = trimmed.split(",");
        List<T> genericList = new ArrayList<>();
        for(String element : split) {
            parameter.setValueStr(element);
            genericList.add(parameter.execute(server,sender));
        }
        return genericList;
    }
}

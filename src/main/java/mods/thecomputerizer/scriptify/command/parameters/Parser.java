package mods.thecomputerizer.scriptify.command.parameters;

import net.minecraft.command.CommandException;

import java.util.ArrayList;
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

    public static double[] parseDoubleArray(Parameter<?> parameter, String val) throws CommandException {
        if(!val.startsWith("[") || !val.endsWith("]")) parameter.throwGeneric("array",parameter.getName(),val);
        String trimmed = val.trim().substring(1);
        trimmed = trimmed.substring(0,trimmed.length()-1);
        String[] split = trimmed.split(",");
        if(split.length==0) return new double[0];
        double[] ret = new double[split.length];
        for(int i=0; i<ret.length; i++) ret[i] = parseDouble(parameter,split[i]);
        return ret;
    }

    public static int[] parseIntArray(Parameter<?> parameter, String val) throws CommandException {
        if(!val.startsWith("[") || !val.endsWith("]")) parameter.throwGeneric("array",parameter.getName(),val);
        String trimmed = val.trim().substring(1);
        trimmed = trimmed.substring(0,trimmed.length()-1);
        String[] split = trimmed.split(",");
        if(split.length==0) return new int[0];
        int[] ret = new int[split.length];
        for(int i=0; i<ret.length; i++) ret[i] = parseInt(parameter,split[i]);
        return ret;
    }

    public static String[] parseStringArray(Parameter<?> parameter, String val) throws CommandException {
        if(!val.startsWith("[") || !val.endsWith("]")) parameter.throwGeneric("array",parameter.getName(),val);
        String trimmed = val.trim().substring(1);
        trimmed = trimmed.substring(0,trimmed.length()-1);
        if(trimmed.isEmpty()) return new String[0];
        if(!trimmed.contains("\"")) return trimmed.split(",");
        List<Integer> indices = new ArrayList<>();
        indices.add(0);
        indices = findCommaIndices(indices,trimmed,0,0,false);
        if(indices.size()==1) return new String[]{trimmed};
        List<String> ret = new ArrayList<>();
        for(int i=0; i<indices.size(); i++) {
            if(i+1==indices.size()-1) ret.add(trimmed.substring(indices.get(i)));
            else ret.add(trimmed.substring(indices.get(i),indices.get(i+1)-1));
        }
        return ret.toArray(new String[0]);
    }

    private static List<Integer> findCommaIndices(List<Integer> indices, String val, int index, int quoteCounter, boolean prevEsc) {
        char c = val.charAt(0);
        if(c=='"') {
            quoteCounter = prevEsc ? quoteCounter : quoteCounter+1;
            prevEsc = false;
        } else if(c==',') {
            if(quoteCounter%2==0) indices.add(index);
            prevEsc = false;
        }
        else prevEsc = c == '\\';
        index++;
        return val.length()==1 ? indices : findCommaIndices(indices,val.substring(1),index,quoteCounter,prevEsc);
    }
}

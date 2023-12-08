package mods.thecomputerizer.scriptify.command.parameters;

import mods.thecomputerizer.scriptify.command.parameters.common.ParameterDouble;
import mods.thecomputerizer.scriptify.command.parameters.common.ParameterNumber;
import net.minecraft.command.CommandException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Parser {

    public static boolean parseBool(String val) {
        return Boolean.parseBoolean(val.trim().toLowerCase());
    }

    public static <N extends ParameterNumber<?>> byte parseByte(N parameter, String val) throws CommandException {
        return parseByte(parameter,val,(byte)0);
    }

    public static <N extends ParameterNumber<?>> byte parseByte(N parameter, String val, byte base) throws CommandException {
        return parseNumber(parameter,val,base,Byte::parseByte,(a1,a2) -> (byte)(a1.byteValue()+a2.byteValue()));
    }

    public static <D extends ParameterDouble> double parseDouble(D parameter, String val) throws CommandException {
        return parseDouble(parameter,val,0d);
    }

    public static <D extends ParameterDouble> double parseDouble(D parameter, String val, double base) throws CommandException {
        return parseNumber(parameter,val,base,Double::parseDouble,(a1,a2) -> a1.doubleValue()+a2.doubleValue());
    }

    public static <N extends ParameterNumber<?>> float parseFloat(N parameter, String val) throws CommandException {
        return parseFloat(parameter,val,0f);
    }

    public static <N extends ParameterNumber<?>> float parseFloat(N parameter, String val, float base) throws CommandException {
        return parseNumber(parameter,val,base,Float::parseFloat,(a1,a2) -> a1.floatValue()+a2.floatValue());
    }

    public static <N extends ParameterNumber<?>> int parseInt(N parameter, String val) throws CommandException {
        return parseInt(parameter,val,0);
    }

    public static <N extends ParameterNumber<?>> int parseInt(N parameter, String val, int base) throws CommandException {
        return parseNumber(parameter,val,base,Integer::parseInt,(a1,a2) -> a1.intValue()+a2.intValue());
    }

    public static <N extends ParameterNumber<?>> long parseLong(N parameter, String val) throws CommandException {
        return parseLong(parameter,val,0);
    }

    public static <N extends ParameterNumber<?>> long parseLong(N parameter, String val, long base) throws CommandException {
        return parseNumber(parameter,val,base,Long::parseLong,(a1,a2) -> a1.longValue()+a2.longValue());
    }

    private static <N extends Number,P extends ParameterNumber<?>> N parseNumber(
            P parameter, String val, N base, Function<String,N> parserFunc,
            BiFunction<Number,Number,N> adderFunc) throws CommandException {
        try {
            String unparsed = val.trim();
            if(val.contains("~")) unparsed = unparsed.replaceAll("~","");
            else base = adderFunc.apply(0,0);
            N parsed = parserFunc.apply(unparsed);
            return adderFunc.apply(parsed,base);
        } catch(NumberFormatException ex) {
            parameter.throwGeneric("number",parameter.getName(),val);
            return adderFunc.apply(0,0);
        }
    }

    public static <N extends ParameterNumber<?>> short parseShort(N parameter, String val) throws CommandException {
        return parseShort(parameter,val,(short)0);
    }

    public static <N extends ParameterNumber<?>> short parseShort(N parameter, String val, short base) throws CommandException {
        return parseNumber(parameter,val,base,Short::parseShort,(a1,a2) -> (short)(a1.shortValue()+a2.shortValue()));
    }

    public static <T> List<T> parseArray(Parameter<T> parameter, String val) throws CommandException {
        List<T> list = new ArrayList<>();
        if(!val.startsWith("[") || !val.endsWith("]")) {
            parameter.setValueStr(val);
            addToList(list,parameter.parse());
        }
        else {
            String trimmed = val.trim().substring(1);
            trimmed = trimmed.substring(0, trimmed.length() - 1);
            String[] split = trimmed.split(",");
            for(String element : split) {
                parameter.setValueStr(element);
                addToList(list,parameter.parse());
            }
        }
        return list;
    }

    private static <T> void addToList(List<T> list, @Nullable T val) {
        String str = Objects.nonNull(val) ? val.toString() : null;
        if(StringUtils.isNotBlank(str)) list.add(val);
    }
}

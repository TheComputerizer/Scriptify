package mods.thecomputerizer.scriptify.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Patterns {
    public static final Pattern AMOUNT = Pattern.compile(">\\*(-?[0-9]]+)");
    public static final Pattern ARRAY_DEF = Pattern.compile("(\\[*]*)");
    public static final Pattern BEP = Pattern.compile("<([a-z0-9_\\-:]+)>",Pattern.CASE_INSENSITIVE);
    public static final Pattern PARAMETER = Pattern.compile("([a-z0-9_\\-:\\[\\]]+)=([a-z0-9_\\-:\\[\\]]+)",Pattern.CASE_INSENSITIVE);

    public static Matcher getMatcher(CharSequence str, String regex) {
        return getMatcher(str,regex,0);
    }

    public static Matcher getMatcher(CharSequence str, String regex, int patternFlags) {
        return Pattern.compile(regex,patternFlags).matcher(str);
    }

    public static boolean matchesAny(String original, String ... matches) {
        return matchesAny(original,false, Arrays.asList(matches));
    }

    public static boolean matchesAny(String original, Iterable<String> matches) {
        return matchesAny(original,false,matches);
    }

    public static boolean matchesAny(String original, boolean matchCase, String ... matches) {
        return matchesAny(original,matchCase,Arrays.asList(matches));
    }

    public static boolean matchesAny(String original, boolean matchCase, Iterable<String> matches) {
        if(StringUtils.isBlank(original)) return true;
        original = matchCase ? original : original.toLowerCase();
        for(String match : matches) {
            if(StringUtils.isBlank(match)) return true;
            match = matchCase ? match : match.toLowerCase();
            if(original.matches(match)) return true;
        }
        return false;
    }
}

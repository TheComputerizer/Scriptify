package mods.thecomputerizer.scriptify.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Common helper methods for tab completions for both commands and parameters
 */
public class TabCompletions {

    public static List<String> getStrictOptions(String prefix, String arg, String ... options) {
        return getStrictOptions(prefix,arg,Arrays.asList(options));
    }

    public static List<String> getStrictOptions(String prefix, String arg, Iterable<String> options) {
        List<String> completions = new ArrayList<>();
        for(String option : options)
            if(option.startsWith(arg)) completions.add(prefix+option);
        if(completions.isEmpty())
            for(String option : options)
                completions.add(prefix+option);
        return completions;
    }
}

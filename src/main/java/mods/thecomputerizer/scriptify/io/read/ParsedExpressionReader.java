package mods.thecomputerizer.scriptify.io.read;

import mods.thecomputerizer.scriptify.io.IOUtils;
import mods.thecomputerizer.scriptify.mixin.access.*;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import stanhebben.zenscript.parser.expression.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParsedExpressionReader implements IClampedStringReader<String> {

    /**
     * Copies the input expression into the input list of strings. Returns a list of required imports.
     */
    public static Set<String> copy(List<String> lines, ParsedExpression expression) {
        return copy(lines,expression,false);
    }

    public static Set<String> copy(List<String> lines, ParsedExpression expression, boolean isMapVal) {
        ParsedExpressionReader reader = new ParsedExpressionReader(expression,isMapVal);
        reader.copy(lines);
        return reader.classNames;
    }

    /**
     * Inner copy method for passing up import lists
     */
    private static void copyInner(Set<String> imports, List<String> lines, ParsedExpression expression, boolean isMapVal) {
        ParsedExpressionReader reader = new ParsedExpressionReader(expression,isMapVal);
        reader.classNames.addAll(imports);
        reader.copy(lines);
        imports.addAll(reader.classNames);
    }

    /**
     * Inner copy method for returning a string instead
     */
    private static String copyInner(Set<String> imports, ParsedExpression expression, boolean isMapVal) {
        ParsedExpressionReader reader = new ParsedExpressionReader(expression,isMapVal);
        reader.classNames.addAll(imports);
        String ret = reader.toString();
        imports.addAll(reader.classNames);
        return ret;
    }

    private final ParsedExpression expression;
    private final boolean isMapVal;
    private final Set<String> classNames;

    public ParsedExpressionReader(ParsedExpression expression) {
        this(expression,false);
    }

    public ParsedExpressionReader(ParsedExpression expression, boolean isMapVal) {
        this.expression = expression;
        this.isMapVal = isMapVal;
        this.classNames = new HashSet<>();
    }

    public void copy(List<String> lines) {
        if(this.expression instanceof ParsedExpressionCall) {
            ParsedExpressionCallAccessor access = (ParsedExpressionCallAccessor)this.expression;
            List<ParsedExpression> args = access.getArguments();
            new ParsedExpressionReader(access.getReceiver()).copy(lines);
            for(int i=0; i<args.size(); i++) {
                copyInner(this.classNames,lines,args.get(i),this.isMapVal);
                if(i+1<args.size()) lines.add(", ");
            }
            lines.add(")");
        } else if(this.expression instanceof ParsedExpressionMember) {
            ParsedExpressionMemberAccessor access = (ParsedExpressionMemberAccessor)this.expression;
            String method = copyInner(this.classNames,access.getValue(),this.isMapVal)+"."+access.getMember()+"(";
            lines.add(method);
        } else if(this.expression instanceof ParsedExpressionValue) {
            new PartialExpressionReader(((ParsedExpressionValueAccessor)this.expression).getValue(),this.isMapVal).copy(lines);
        } else if(this.expression instanceof ParsedExpressionArray) {
            List<ParsedExpression> contents = ((ParsedExpressionArrayAccessor)this.expression).getContents();
            lines.add("[");
            for(int i=0; i<contents.size(); i++) {
                copyInner(this.classNames,lines,contents.get(i),this.isMapVal);
                if(i+1<contents.size()) lines.add(", ");
            }
            lines.add("]");
        } else if(this.expression instanceof ParsedExpressionVariable) {
            ParsedExpressionVariableAccessor access = (ParsedExpressionVariableAccessor)this.expression;
            this.classNames.add(access.getName());
            lines.add(access.getName());
        } else if(this.expression instanceof ParsedExpressionBinary) {
            ParsedExpressionBinaryAccessor access = (ParsedExpressionBinaryAccessor)this.expression;
            lines.add(copyInner(this.classNames,access.getLeft(),this.isMapVal)+IOUtils.writeOperator(access.getOperator())+
                    copyInner(this.classNames,access.getRight(),this.isMapVal));
        } else if(this.expression instanceof ParsedExpressionMap) {
            ParsedExpressionMapAccessor access = (ParsedExpressionMapAccessor)this.expression;
            lines.add("{");
            for(int i=0; i<access.getKeys().size(); i++) {
                copyInner(this.classNames,lines,access.getKeys().get(i),this.isMapVal);
                lines.add(": ");
                copyInner(this.classNames,lines,access.getValues().get(i),true);
                if(i+1<access.getKeys().size()) lines.add(",");
            }
            lines.add("}");
        } else if(this.expression instanceof ParsedExpressionCast) {
            ParsedExpressionCastAccessor access = (ParsedExpressionCastAccessor)this.expression;
            lines.add(copyInner(this.classNames,access.getValue(),this.isMapVal)+" as "+access.getType().getName());
        }
    }

    @Override
    public String parse(String unparsed) {
        return null;
    }

    @Override
    public String toString() {
        List<String> lines = new ArrayList<>();
        copy(lines);
        return TextUtil.listToString(lines,"");
    }
}

package mods.thecomputerizer.scriptify.io.read;

import mods.thecomputerizer.scriptify.Scriptify;
import mods.thecomputerizer.scriptify.io.IOUtils;
import mods.thecomputerizer.scriptify.mixin.mods.ExpressionCallStaticAccessor;
import mods.thecomputerizer.scriptify.mixin.mods.ExpressionIntAccessor;
import mods.thecomputerizer.scriptify.mixin.mods.ExpressionStringAccessor;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import stanhebben.zenscript.expression.Expression;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionInt;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.expression.partial.IPartialExpression;
import stanhebben.zenscript.parser.expression.ParsedExpression;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PartialExpressionReader implements IClampedStringReader<String> {

    private final IPartialExpression expression;
    private final boolean isMapVal;

    public PartialExpressionReader(IPartialExpression expression) {
        this(expression,false);
    }

    public PartialExpressionReader(IPartialExpression expression, boolean isMapVal) {
        this.expression = expression;
        this.isMapVal = isMapVal;
    }

    @Override
    public void copy(List<String> lines) {
        Scriptify.logInfo("Partial expression class is {}",this.expression.getClass().getName());
        if(this.expression instanceof ExpressionCallStatic) {
            ExpressionCallStaticAccessor access = (ExpressionCallStaticAccessor)this.expression;
            lines.add(IOUtils.getWriterFunc(this.expression.getType().getName()).apply(access.getArguments()));
        }
        else if(this.expression instanceof ExpressionInt)
            lines.add(String.valueOf(((ExpressionIntAccessor)this.expression).getValue()));
        else if(this.expression instanceof ExpressionString) {
            String val = ((ExpressionStringAccessor)this.expression).getValue();
            if(this.isMapVal) val = "\""+val+"\"";
            lines.add(val);
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

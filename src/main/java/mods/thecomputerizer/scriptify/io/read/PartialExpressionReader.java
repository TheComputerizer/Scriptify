package mods.thecomputerizer.scriptify.io.read;

import mods.thecomputerizer.scriptify.io.write.FileWriter;
import mods.thecomputerizer.scriptify.util.IOUtils;
import mods.thecomputerizer.scriptify.mixin.access.ExpressionCallStaticAccessor;
import mods.thecomputerizer.scriptify.mixin.access.ExpressionIntAccessor;
import mods.thecomputerizer.scriptify.mixin.access.ExpressionStringAccessor;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import stanhebben.zenscript.expression.ExpressionCallStatic;
import stanhebben.zenscript.expression.ExpressionInt;
import stanhebben.zenscript.expression.ExpressionString;
import stanhebben.zenscript.expression.partial.IPartialExpression;

import java.util.ArrayList;
import java.util.List;

public class PartialExpressionReader implements FileReader<String> {

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
        if(this.expression instanceof ExpressionCallStatic) {
            ExpressionCallStaticAccessor access = (ExpressionCallStaticAccessor)this.expression;
            FileWriter writer = IOUtils.getWriterFunc(this.expression.getType().getName()).apply(access.getArguments());
            writer.writeLines(lines);
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

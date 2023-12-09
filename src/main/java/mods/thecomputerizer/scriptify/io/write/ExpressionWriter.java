package mods.thecomputerizer.scriptify.io.write;

import crafttweaker.api.item.IIngredient;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.io.data.BEP;
import mods.thecomputerizer.scriptify.mixin.access.*;
import mods.thecomputerizer.scriptify.util.IOUtils;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.expression.*;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.type.natives.IJavaMethod;

import java.util.List;

public class ExpressionWriter extends PartialWriter<Expression> {


    public ExpressionWriter(int tabLevel) {
        super(tabLevel);
    }

    private boolean isBEPAmount(IJavaMethod method) {
        ScriptifyRef.LOGGER.error("CHECKING IF VIRTUAL IS CORRECT");
        Class<?> returnClass = IOUtils.getClassFromAlias(method.getReturnType().getName());
        if(IIngredient.class.isAssignableFrom(returnClass) || ItemStack.class.isAssignableFrom(returnClass)) {
            ScriptifyRef.LOGGER.error("RETURN CLASS IS CORRECT");
            if(method.getParameterTypes().length>0 && method.getParameterTypes()[0]==ZenType.INT) {
                ScriptifyRef.LOGGER.error("PARAMENTERS CLASS IS CORRECT");
                ScriptifyRef.LOGGER.error("METHOD CLASS IS {}",method.getClass());
                return true;
            } else ScriptifyRef.LOGGER.error("PARAMENTERS CLASS IS NOT CORRECT");
        } else ScriptifyRef.LOGGER.error("RETURN CLASS IS NOT CORRECT {}",returnClass);
        return false;
    }

    private String writeArgs(String split, Expression ... args) {
        String[] elementStrs = new String[args.length];
        for(int i=0; i<elementStrs.length; i++) elementStrs[i] = writeExpression(args[i]);
        return TextUtil.arrayToString(split,(Object[])elementStrs);
    }

    private String writeArray(ExpressionArray array) {
        return "["+writeArgs(",",((ExpressionArrayAccessor)array).getContents())+"]";
    }

    private String writeExpression(Expression expression) {
        if(expression instanceof ExpressionArray) return writeArray((ExpressionArray)expression);
        if(expression instanceof ExpressionMap) return writeMap((ExpressionMap)expression);
        if(expression instanceof ExpressionCallStatic) {
            String written = writeArgs(":",((ExpressionCallStaticAccessor)expression).getArguments());
            if(expression.getType().getName().contains("ore")) written = "ore:"+written;
            else if(expression.getType().getName().contains("liquid")) written = "liquid:"+written;
            ScriptifyRef.LOGGER.error("BEP WRITING FROM {}",written);
            return BEP.of(written).toString();
        }
        if(expression instanceof ExpressionCallVirtual) return writeVirtual((ExpressionCallVirtual)expression);
        return writePrimitiveExpression(expression);
    }

    @Override
    public void writeLines(List<String> lines) {
        tryAppend(lines,writeExpression(getElement()),true);
    }

    private String writeMap(ExpressionMap map) {
        ExpressionMapAccessor access = (ExpressionMapAccessor)map;
        Expression[] keys = access.getKeys();
        Expression[] values = access.getValues();
        StringBuilder builder = new StringBuilder("{");
        for(int i=0; i<keys.length; i++) {
            builder.append(keys[i]).append(":").append(values[i]);
            if(i+1<keys.length) builder.append(",");
        }
        return builder.append("}").toString();
    }

    private String writePrimitiveExpression(Expression expression) {
        Object obj = "null";
        if(expression instanceof ExpressionBool) obj = ((ExpressionBoolAccessor)expression).getValue();
        else if(expression instanceof ExpressionFloat) obj = ((ExpressionFloatAccessor)expression).getValue();
        else if(expression instanceof ExpressionInt) obj = ((ExpressionIntAccessor)expression).getValue();
        else if(expression instanceof ExpressionString) obj = ((ExpressionStringAccessor)expression).getValue();
        else ScriptifyRef.LOGGER.error("UNKNOWN EXPRESSION CLASS {}",expression.getClass().getName());
        return obj.toString();
    }

    private String writeVirtual(ExpressionCallVirtual call) {
        ExpressionCallVirtualAccessor access = (ExpressionCallVirtualAccessor)call;
        String receiver = writeExpression(access.getReceiver());
        String args = writeArgs(":",access.getArguments());
        return isBEPAmount(access.getMethod()) ? receiver+"*"+args : receiver+".("+args+")";
    }
}

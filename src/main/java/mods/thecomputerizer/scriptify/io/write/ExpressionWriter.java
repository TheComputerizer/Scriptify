package mods.thecomputerizer.scriptify.io.write;

import crafttweaker.api.item.IItemStack;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.mixin.access.*;
import mods.thecomputerizer.theimpossiblelibrary.util.GenericUtils;
import stanhebben.zenscript.expression.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Setter @Getter
public class ExpressionWriter extends FileWriter {

    private Expression expression;
    private boolean disableStringQuotes;

    public ExpressionWriter(Expression expression) {
        this(0);
        this.expression = expression;
    }

    public ExpressionWriter(int tabLevel) {
        super(tabLevel);
    }

    private ClampedWriter getArrayWriter(Expression[] expressions, boolean elementNewLine) {
        ClampedWriter writer = new ClampedWriter(getTabLevel()+1);
        writer.setStrOpen("[");
        writer.setStrClose(" ]");
        writer.setNeedsSemicolon(false);
        writer.addWriters(ex -> {
            ExpressionWriter elementWriter = new ExpressionWriter(ex);
            elementWriter.setDisableStringQuotes(this.disableStringQuotes);
            elementWriter.setNewLine(elementNewLine);
            return elementWriter;
        },expressions);
        return writer;
    }

    private @Nullable ClampedWriter getClampedWriter() {
        if(this.expression instanceof ExpressionArray)
            return getArrayWriter(((ExpressionArrayAccessor)this.expression).getContents(),true);
        if(this.expression instanceof ExpressionMap)
            return getMapWriter((ExpressionMap)this.expression);
        if(this.expression instanceof ExpressionCallStatic)
            return getStaticWriter((ExpressionCallStatic)this.expression);
        if(this.expression instanceof ExpressionCallVirtual)
            return getVirtualWriter((ExpressionCallVirtual)expression);
        else {
            ScriptifyRef.LOGGER.error("UNKNOWN EXPRESSION CLASS {}",this.expression.getClass().getName());
            return null;
        }
    }

    private ClampedWriter getMapWriter(ExpressionMap map) {
        ClampedWriter writer = new ClampedWriter(getTabLevel()+1);
        writer.setStrOpen("{");
        writer.setStrClose("}");
        writer.setNeedsSemicolon(false);
        ExpressionMapAccessor acces = (ExpressionMapAccessor)map;
        Expression[] keys = acces.getKeys();
        Expression[] values = acces.getValues();
        for(int i=0; i<keys.length; i++) {
            ClampedWriter entryWriter = new ClampedWriter(writer.getTabLevel()+1);
            entryWriter.setStrOpen("");
            entryWriter.setStrClose("");
            entryWriter.setStrSeparator(":");
            entryWriter.setNeedsSemicolon(false);
            entryWriter.setForceAppend(true);
            entryWriter.setCharacterLimit(writer.getCharacterLimit());
            ExpressionWriter keyWriter = new ExpressionWriter(keys[i]);
            ExpressionWriter valueWriter = new ExpressionWriter(values[i]);
            entryWriter.setNewLine(GenericUtils.isAnyType(keyWriter.getExpression(),ExpressionArray.class,ExpressionMap.class) ||
                    GenericUtils.isAnyType(valueWriter.getExpression(),ExpressionArray.class,ExpressionMap.class));
            keyWriter.setDisableStringQuotes(this.disableStringQuotes);
            valueWriter.setDisableStringQuotes(false);
            keyWriter.setCharacterLimit(entryWriter.getCharacterLimit());
            valueWriter.setCharacterLimit(entryWriter.getCharacterLimit());
            entryWriter.addWriter(keyWriter);
            entryWriter.addWriter(valueWriter);
            writer.addWriter(entryWriter);
        }
        return writer;
    }

    private PartialWriter<?> getPrimitiveWriter() {
        String strVal;
        if(this.expression instanceof ExpressionBool)
            strVal = String.valueOf(((ExpressionBoolAccessor)this.expression).getValue());
        else if(this.expression instanceof ExpressionFloat)
            strVal = String.valueOf(((ExpressionFloatAccessor)this.expression).getValue());
        else if(this.expression instanceof ExpressionInt)
            strVal = String.valueOf(((ExpressionIntAccessor)this.expression).getValue());
        else if(this.expression instanceof ExpressionString) {
            strVal = ((ExpressionStringAccessor)this.expression).getValue();
            if(!this.disableStringQuotes) strVal = "\""+strVal+"\"";
        }
        else {
            ScriptifyRef.LOGGER.error("UNKNOWN EXPRESSION CLASS {}",this.expression.getClass().getName());
            strVal = "null";
        }
        PartialWriter<String> writer = new PartialWriter<>(this.getTabLevel()+1);
        writer.setElement(strVal);
        return writer;
    }

    private ClampedWriter getStaticWriter(ExpressionCallStatic call) {
        ClampedWriter writer = new ClampedWriter(getTabLevel()+1);
        String typeName = call.getType().getName().toLowerCase();
        Expression[] args = ((ExpressionCallStaticAccessor)call).getArguments();
        if(typeName.contains("liquid"))
            args = new Expression[]{new ExpressionString(call.getPosition(),"liquid"),args[0]};
        else if(typeName.contains("ore"))
            args = new Expression[]{new ExpressionString(call.getPosition(),"ore"),args[0]};
        for(Expression arg : args) {
            ExpressionWriter argWriter = new ExpressionWriter(arg);
            argWriter.setTabLevel(writer.getTabLevel()+1);
            argWriter.setDisableStringQuotes(true);
            writer.setDisableSpaces(true);
            argWriter.setNewLine(false);
            ScriptifyRef.LOGGER.error("{} {} {}",Objects.isNull(writer),Objects.isNull(argWriter),Objects.isNull(arg));
            if(!argWriter.toString().trim().matches("0")) writer.addWriter(argWriter);
        }
        writer.setTabLevel(getTabLevel()+1);
        writer.setStrOpen("<");
        writer.setStrClose(">");
        writer.setStrSeparator(":");
        writer.setDisableSpaces(true);
        writer.setForceAppend(true);
        writer.setNeedsSemicolon(false);
        return writer;
    }

    private ClampedWriter getVirtualWriter(ExpressionCallVirtual virtual) {
        ExpressionCallVirtualAccessor access = (ExpressionCallVirtualAccessor)virtual;
        Expression receiver = access.getReceiver();
        ExpressionWriter receiverWriter = new ExpressionWriter(access.getReceiver());
        receiverWriter.setTabLevel(getTabLevel()+1);
        ClampedWriter writer = getArrayWriter(access.getArguments(),false);
        writer.setStrOpen("");
        writer.setStrClose("");
        writer.setPrefix(receiverWriter+(receiver instanceof ExpressionCallStatic ? "*" : "."));
        writer.setForceAppend(true);
        writer.setDisableSpaces(true);
        return writer;
    }

    @Override
    public void writeLines(List<String> lines) {
        FileWriter writer = GenericUtils.isAnyType(this.expression, ExpressionBool.class,ExpressionFloat.class,ExpressionInt.class,
                ExpressionString.class) ? getPrimitiveWriter() : getClampedWriter();
        if(Objects.nonNull(writer)) writer.writeLines(lines);
    }
}

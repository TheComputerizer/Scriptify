package mods.thecomputerizer.scriptify.io.write;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.io.data.ExpressionCallHolder;
import mods.thecomputerizer.scriptify.io.data.ExpressionCastHolder;
import mods.thecomputerizer.scriptify.mixin.access.*;
import mods.thecomputerizer.theimpossiblelibrary.util.GenericUtils;
import org.apache.commons.lang3.StringUtils;
import stanhebben.zenscript.expression.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Setter @Getter
public class ExpressionWriter extends FileWriter {

    private Expression expression;
    private boolean disableStringQuotes;
    private FileWriter cachedSubWriter;

    public ExpressionWriter(Expression expression) {
        this(0);
        this.expression = expression;
    }

    public ExpressionWriter(int tabLevel) {
        super(tabLevel);
    }

    @Override
    public void collectImports(Set<String> imports) {
        if(this.expression instanceof ExpressionCastHolder) {
            String importName = ((ExpressionCastHolder)this.expression).getImportName();
            if(StringUtils.isNotEmpty(importName)) imports.add(importName);
        }
        if(Objects.nonNull(this.cachedSubWriter)) this.cachedSubWriter.collectImports(imports);
    }

    @Override
    public void collectPreprocessors(Set<String> preprocessors) {
        if(Objects.nonNull(this.cachedSubWriter)) this.cachedSubWriter.collectPreprocessors(preprocessors);
    }

    private ClampedWriter getArrayWriter(Expression[] expressions, boolean ignoreNewLine) {
        ClampedWriter writer = new ClampedWriter(getTabLevel());
        writer.setStrOpen("[");
        writer.setStrClose(" ]");
        writer.setNeedsSemicolon(false);
        writer.addWriters(ex -> {
            ExpressionWriter elementWriter = new ExpressionWriter(ex);
            elementWriter.setDisableStringQuotes(this.disableStringQuotes);
            if(!ignoreNewLine && elementWriter.isArrayOrMap()) elementWriter.setNewLine(true);
            return elementWriter;
        },expressions);
        return writer;
    }

    private FileWriter getCastWriter(ExpressionCastHolder holder) {
        Expression base = holder.getExpression();
        ClampedWriter writer = new ClampedWriter(getTabLevel());
        writer.setStrSeparator("");
        writer.setNeedsSemicolon(false);
        writer.setDisableSpaces(true);
        ExpressionWriter baseWriter = new ExpressionWriter(base);
        baseWriter.setNewLine(false);
        baseWriter.setCharacterLimit(Integer.MAX_VALUE);
        PartialWriter<String> qualifier = new PartialWriter<>(getTabLevel());
        qualifier.setNewLine(false);
        qualifier.setCharacterLimit(Integer.MAX_VALUE);
        qualifier.setElement(holder.getQualifier());
        writer.addWriter(baseWriter);
        writer.addWriter(qualifier);
        return writer;
    }

    private @Nullable ClampedWriter getClampedWriter() {
        if(this.expression instanceof ExpressionArray)
            return getArrayWriter(((ExpressionArrayAccessor) this.expression).getContents(),false);
        if(this.expression instanceof ExpressionMap)
            return getMapWriter((ExpressionMap)this.expression);
        if(this.expression instanceof ExpressionCallStatic)
            return getStaticWriter((ExpressionCallStatic)this.expression);
        if(this.expression instanceof ExpressionCallVirtual)
            return getVirtualWriter((ExpressionCallVirtual)expression);
        if(this.expression instanceof ExpressionCallHolder)
            return getHolderWriter((ExpressionCallHolder)this.expression);
        else {
            ScriptifyRef.LOGGER.error("UNKNOWN EXPRESSION CLASS {}",this.expression.getClass().getName());
            return null;
        }
    }

    private ClampedWriter getHolderWriter(ExpressionCallHolder holder) {
        ClampedWriter writer = new ClampedWriter(getTabLevel());
        writer.setPrefix((new ExpressionWriter(holder.getReceiver())+"."+holder.getMethodName()).replaceAll(" ",""));
        writer.setDisableSpaces(true);
        writer.setNeedsSemicolon(false);
        writer.setCharacterLimit(getCharacterLimit()*2);
        writer.addWriters(ExpressionWriter::new,holder.getArguments());
        return writer;
    }

    private ClampedWriter getMapWriter(ExpressionMap map) {
        ClampedWriter writer = new ClampedWriter(getTabLevel());
        writer.setStrOpen("{");
        writer.setStrClose(" }");
        writer.setNeedsSemicolon(false);
        ExpressionMapAccessor access = (ExpressionMapAccessor)map;
        Expression[] keys = access.getKeys();
        Expression[] values = access.getValues();
        for(int i=0; i<keys.length; i++) {
            ClampedWriter entryWriter = new ClampedWriter(writer.getTabLevel()+1);
            entryWriter.setStrOpen("");
            entryWriter.setStrClose("");
            entryWriter.setStrSeparator(": ");
            entryWriter.setNeedsSemicolon(false);
            entryWriter.setDisableSpaces(true);
            entryWriter.setCharacterLimit(writer.getCharacterLimit()*2);
            ExpressionWriter keyWriter = new ExpressionWriter(keys[i]);
            ExpressionWriter valueWriter = new ExpressionWriter(values[i]);
            entryWriter.setNewLine(keyWriter.isArrayOrMap() || valueWriter.isArrayOrMap());
            keyWriter.setDisableStringQuotes(true);
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
        writer.setCharacterLimit(getCharacterLimit());
        return writer;
    }

    private ClampedWriter getStaticWriter(ExpressionCallStatic call) {
        ClampedWriter writer = new ClampedWriter(getTabLevel());
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
            argWriter.setCharacterLimit(Integer.MAX_VALUE);
            if(!argWriter.toString().trim().matches("0")) writer.addWriter(argWriter);
        }
        writer.setTabLevel(getTabLevel()+1);
        writer.setStrOpen("<");
        writer.setStrClose(">");
        writer.setStrSeparator(":");
        writer.setDisableSpaces(true);
        writer.setCharacterLimit(Integer.MAX_VALUE);
        writer.setNeedsSemicolon(false);
        return writer;
    }

    private ClampedWriter getVirtualWriter(ExpressionCallVirtual virtual) {
        ExpressionCallVirtualAccessor access = (ExpressionCallVirtualAccessor)virtual;
        Expression receiver = access.getReceiver();
        ExpressionWriter receiverWriter = new ExpressionWriter(receiver);
        receiverWriter.setTabLevel(getTabLevel());
        ClampedWriter writer = getArrayWriter(access.getArguments(),true);
        writer.setStrOpen("");
        writer.setStrClose("");
        writer.setPrefix(receiverWriter+(receiver instanceof ExpressionCallStatic ? "*" : "."));
        writer.setDisableSpaces(true);
        writer.setCharacterLimit(getCharacterLimit()*2);
        return writer;
    }

    public boolean isArrayOrMap() {
        return this.expression instanceof ExpressionArray || this.expression instanceof ExpressionMap;
    }

    @Override
    public void writeLines(List<String> lines) {
        FileWriter writer;
        if(this.expression instanceof ExpressionCastHolder) writer = getCastWriter((ExpressionCastHolder)this.expression);
        else if(GenericUtils.isAnyType(this.expression,ExpressionBool.class,ExpressionFloat.class,ExpressionInt.class,
                ExpressionString.class)) writer = getPrimitiveWriter();
        else writer = getClampedWriter();
        this.cachedSubWriter = writer;
        if(Objects.nonNull(writer)) {
            writer.setNewLine(isNewLine());
            writer.setTabLevel(getTabLevel());
            writer.writeLines(lines);
        }
    }
}

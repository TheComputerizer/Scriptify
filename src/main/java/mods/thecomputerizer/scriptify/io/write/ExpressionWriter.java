package mods.thecomputerizer.scriptify.io.write;

import crafttweaker.api.data.IData;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.io.IOUtils;
import mods.thecomputerizer.scriptify.io.data.BEP;
import mods.thecomputerizer.scriptify.io.data.ExpressionCallHolder;
import mods.thecomputerizer.scriptify.io.data.ExpressionCastHolder;
import mods.thecomputerizer.scriptify.io.read.ExpressionReader;
import mods.thecomputerizer.scriptify.mixin.access.*;
import mods.thecomputerizer.scriptify.util.iterator.Wrapperable;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.theimpossiblelibrary.util.GenericUtils;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import stanhebben.zenscript.expression.*;
import stanhebben.zenscript.type.ZenType;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
@Setter @Getter
public class ExpressionWriter extends FileWriter {

    public static ExpressionWriter make(Expression expression, Consumer<ExpressionWriter> settings) {
        ExpressionWriter writer = new ExpressionWriter(expression);
        settings.accept(writer);
        return writer;
    }

    public static ExpressionWriter makeAndCache(ExpressionReader reader) {
        return make(reader.getExpression(),ExpressionWriter::cache);
    }

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

    public void cache() {
        FileWriter writer;
        if(this.expression instanceof ExpressionCastHolder) writer = getCastWriter((ExpressionCastHolder)this.expression);
        else if(GenericUtils.isAnyType(this.expression,ExpressionBool.class,ExpressionFloat.class,ExpressionInt.class,
                ExpressionString.class)) writer = getPrimitiveWriter();
        else writer = getClampedWriter();
        this.cachedSubWriter = writer;
    }

    private IData castToIData(Object value) {
        return CraftTweakerMC.getIData(writeGenericTag(value));
    }

    private NBTBase writeGenericTag(Object value) {
        if(ClassUtils.isPrimitiveOrWrapper(value.getClass()) || value instanceof String) return writePrimitveTag(value);
        if(value instanceof Iterable<?>) return writeItrTag((Iterable<?>)value);
        if(value instanceof Map<?,?>) return writeMapTag((Map<?,?>)value);
        if(value instanceof byte[]) return new NBTTagByteArray((byte[])value);
        if(value instanceof int[]) return new NBTTagIntArray((int[])value);
        if(value instanceof long[]) return new NBTTagLongArray((long[])value);
        if(value instanceof Object[]) return writeArrayTag((Object[])value);
        return new NBTTagString(value.toString());
    }

    private NBTTagCompound writeMapTag(Map<?,?> map) {
        NBTTagCompound tag = new NBTTagCompound();
        for(Map.Entry<?,?> entry : map.entrySet())
            tag.setTag(entry.getKey().toString(),writeGenericTag(entry.getValue()));
        return tag;
    }

    private NBTTagList writeArrayTag(Object[] array) {
        NBTTagList tag = new NBTTagList();
        for(Object element : array) tag.appendTag(writeGenericTag(element));
        return tag;
    }

    private NBTTagList writeItrTag(Iterable<?> itr) {
        NBTTagList tag = new NBTTagList();
        for(Object element : itr) tag.appendTag(writeGenericTag(element));
        return tag;
    }

    private NBTBase writePrimitveTag(Object primitive) {
        if(primitive instanceof Byte) return new NBTTagByte((byte)primitive);
        if(primitive instanceof Boolean) return new NBTTagByte((byte)(((boolean)primitive) ? 1 : 0));
        if(primitive instanceof Double) return new NBTTagDouble((double)primitive);
        if(primitive instanceof Float) return new NBTTagFloat((float)primitive);
        if(primitive instanceof Integer) return new NBTTagInt((int)primitive);
        if(primitive instanceof Short) return new NBTTagShort((short)primitive);
        return new NBTTagString(primitive.toString());
    }

    @Override
    public void collectImports(Wrapperable<String> imports) {
        if(this.expression instanceof ExpressionCastHolder) {
            String importName = ((ExpressionCastHolder)this.expression).getImportName();
            if(StringUtils.isNotEmpty(importName)) imports.add(importName);
        }
        if(Objects.nonNull(this.cachedSubWriter)) this.cachedSubWriter.collectImports(imports);
    }

    @Override
    public void collectPreprocessors(Wrapperable<String> preprocessors) {
        if(Objects.nonNull(this.cachedSubWriter)) this.cachedSubWriter.collectPreprocessors(preprocessors);
    }

    private Object evaluate(Object invoker, ZenType type, String methodName, List<FileWriter> writers) {
        Class<?>[] argClasses = new Class<?>[writers.size()];
        Misc.supplyArray(argClasses,i -> {
            FileWriter writer = writers.get(i);
            ScriptifyRef.LOGGER.error("WRITER EVALUATES TO {}", writer);
            if(Objects.isNull(writer)) return IOUtils.getClassFromAlias("null");
            String argType = writer instanceof ExpressionWriter ?
                    IOUtils.getBaseTypeName(((ExpressionWriter)writer).expression.getType()) :
                    Objects.nonNull(writer.getValueInner()) ? writer.getValueInner().getClass().getName() : "null";
            return IOUtils.getClassFromAlias(argType);
        });
        Method method = Misc.getMethod(IOUtils.getClassFromAlias(IOUtils.getBaseTypeName(type)),methodName,argClasses);
        if(Objects.nonNull(method)) {
            Object[] argValues = new Object[writers.size()];
            Misc.supplyArray(argValues,i -> writers.get(i).getValueInner());
            return Misc.invokeMethod(method,invoker,argValues);
        }
        return null;
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

    private Object getClampedValue(ClampedWriter writer) {
        if(this.expression instanceof ExpressionCallStatic) return BEP.of(writer.toString());
        if(this.expression instanceof ExpressionCallVirtual) {
            if(writer.getPrefix().endsWith("*")) return BEP.of(writer.toString());
            ExpressionCallVirtualAccessor access = (ExpressionCallVirtualAccessor)this.expression;
            IIngredient ingredient = BEP.of(writer.getPrefix().substring(0,writer.getPrefix().length()-1)).asIIngredient();
            ZenType type = access.getMethod().getReturnType();
            return "null";
        }
        if(this.expression instanceof ExpressionCallHolder) {
            ExpressionCallHolder holder = (ExpressionCallHolder)this.expression;
            ExpressionWriter invoker = new ExpressionWriter(holder.getReceiver());
            invoker.setDisableStringQuotes(true);
            return evaluate(invoker.getValueInner(),holder.getType(),holder.getMethodName(),writer.writers.getAsList());
        }
        if(this.expression instanceof ExpressionArray)
            return ((List<Object>)writer.getValueInner()).toArray(new Object[0]);
        if(this.expression instanceof ExpressionMap) {
            Map<Object,Object> map = new HashMap<>();
            for(FileWriter entryWriter : writer.getWriters()) {
                List<Object> entryPair = (List<Object>)entryWriter.getValueInner();
                map.putIfAbsent(entryPair.get(0),entryPair.get(1));
            }
            return map;
        }
        if(this.expression instanceof ExpressionCastHolder) {
            ExpressionCastHolder holder = (ExpressionCastHolder)this.expression;
            Class<?> castTo = IOUtils.getClassFromAlias(IOUtils.getBaseTypeName(holder.getType()));
            Object value = new ExpressionWriter(holder.getExpression()).getValueInner();
            return IData.class.isAssignableFrom(castTo) && value instanceof Map<?,?> ?
                    castToIData(value) : castTo.cast(value);
        }
        return writer.getValueInner();
    }

    protected Object getValueInner() {
        if(Objects.isNull(this.cachedSubWriter)) cache();
        if(this.cachedSubWriter instanceof ClampedWriter)
            return getClampedValue((ClampedWriter)this.cachedSubWriter);
        //if(this.cachedSubWriter instanceof ExpressionWriter)
            //return getExpressionValue((ExpressionWriter)this.cachedSubWriter);
        return this.cachedSubWriter.getValueInner();
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
        if(Objects.isNull(this.cachedSubWriter)) cache();
        if(Objects.nonNull(this.cachedSubWriter)) {
            this.cachedSubWriter.setNewLine(isNewLine());
            this.cachedSubWriter.setTabLevel(getTabLevel());
            this.cachedSubWriter.writeLines(lines);
        }
    }
}

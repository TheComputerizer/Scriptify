package mods.thecomputerizer.scriptify.io.read;

import crafttweaker.zenscript.GlobalRegistry;
import lombok.Getter;
import mods.thecomputerizer.scriptify.ScriptifyRef;
import mods.thecomputerizer.scriptify.mixin.access.*;
import mods.thecomputerizer.scriptify.util.Misc;
import mods.thecomputerizer.theimpossiblelibrary.util.TextUtil;
import stanhebben.zenscript.compiler.IEnvironmentGlobal;
import stanhebben.zenscript.expression.*;
import stanhebben.zenscript.expression.partial.IPartialExpression;
import stanhebben.zenscript.parser.expression.*;
import stanhebben.zenscript.symbols.IZenSymbol;
import stanhebben.zenscript.symbols.SymbolPackage;
import stanhebben.zenscript.symbols.SymbolType;
import stanhebben.zenscript.type.ZenType;
import stanhebben.zenscript.type.ZenTypeArray;
import stanhebben.zenscript.type.ZenTypeArrayBasic;
import stanhebben.zenscript.type.ZenTypeAssociative;
import stanhebben.zenscript.type.casting.ICastingRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class ExpressionReader implements FileReader<String> {

    private final IEnvironmentGlobal environment;
    private final Expression expression;

    public ExpressionReader(ParsedExpression parsed, IEnvironmentGlobal environment) {
        this.environment = environment;
        this.expression = getExpression(parsed);
        ScriptifyRef.LOGGER.error("PARSED CLASS {}",parsed);
    }

    public ExpressionReader(Expression expression, IEnvironmentGlobal environment) {
        this.environment = environment;
        this.expression = expression;
    }

    private Expression checkCast(Expression ex) {
        if(ex instanceof ExpressionAs) {
            ExpressionAs as = (ExpressionAs)ex;
            ExpressionAsAccessor access = (ExpressionAsAccessor)as;
            return access.getValue().cast(as.getPosition(),this.environment,access.getCastingRule().getResultingType());
        }
        return ex;
    }

    @Override
    public void copy(List<String> lines) {}

    private Expression getExpression(ParsedExpression expression) {
        return testPartial(getPartial(expression));
    }

    private IPartialExpression getPartial(ParsedExpression expression) {
        if(expression instanceof ParsedExpressionArray) return getArrayExpression((ParsedExpressionArray)expression);
        if(expression instanceof ParsedExpressionBinary) return getBinaryExpression((ParsedExpressionBinary)expression);
        if(expression instanceof ParsedExpressionCall) return getCallExpression((ParsedExpressionCall)expression);
        if(expression instanceof ParsedExpressionCast) return getCastExpression((ParsedExpressionCast)expression);
        if(expression instanceof ParsedExpressionMap) return getMapExpression((ParsedExpressionMap)expression);
        if(expression instanceof ParsedExpressionMember) return getMemberExpression((ParsedExpressionMember)expression);
        if(expression instanceof ParsedExpressionValue) return ((ParsedExpressionValueAccessor)expression).getValue();
        return makeNullExpression();
    }

    private Expression getArrayExpression(ParsedExpressionArray array) {
        ParsedExpressionArrayAccessor access = (ParsedExpressionArrayAccessor)array;
        ZenTypeArrayBasic arrayType = ZenType.ANYARRAY;
        Expression[] contents = getExpressionArray(access.getContents());
        if(contents.length>0) arrayType = new ZenTypeArrayBasic(contents[0].getType());
        return predictArrayType(array,arrayType);
    }

    private Expression getBinaryExpression(ParsedExpressionBinary binary) {
        ParsedExpressionBinaryAccessor access = (ParsedExpressionBinaryAccessor)binary;
        Expression left = getExpression(access.getLeft());
        Expression right = getExpression(access.getRight());
        ZenType type = left.getType();
        return type.binary(binary.getPosition(),this.environment,left,right,access.getOperator());
    }

    private Expression getCallExpression(ParsedExpressionCall call) {
        ParsedExpressionCallAccessor access = (ParsedExpressionCallAccessor)call;
        Expression receiver = getExpression(access.getReceiver());
        ZenType type = receiver.getType();
        ScriptifyRef.LOGGER.error("TYPE IS {} and receiver is class {}",type,receiver.getClass());
        for(int i=0; i<access.getArguments().size(); i++) {
            ParsedExpression parsed = access.getArguments().get(i);
            Expression ex;
            if(parsed instanceof ParsedExpressionMap) {
                IZenSymbol symbol = ((SymbolPackage)((SymbolPackage)GlobalRegistry.getRoot().get("crafttweaker")).get("data")).get("IData");
                ex = predict(parsed,((SymbolType)symbol).getType());
            }
            else ex = getExpression(parsed);
            ScriptifyRef.LOGGER.error("ARG PARTIAL CLASS IS TYPE {} AND CLASS {}",ex.getType(),ex.getClass());
            ((ExpressionCallStaticAccessor)receiver).getArguments()[i] = ex;
        }
        return receiver;
    }

    private Expression getCastExpression(ParsedExpressionCast cast) {
        ParsedExpressionCastAccessor access = (ParsedExpressionCastAccessor)cast;
        return getExpression(access.getValue()).cast(cast.getPosition(),this.environment,access.getType());
    }

    private Expression[] getExpressionArray(List<ParsedExpression> expressions) {
        return expressions.stream().map(this::getExpression).toArray(Expression[]::new);
    }

    private Expression getMapExpression(ParsedExpressionMap map) {
        ParsedExpressionMapAccessor access = (ParsedExpressionMapAccessor)map;
        Expression[] keys = getExpressionArray(access.getKeys());
        Expression[] values = getExpressionArray(access.getValues());
        ZenTypeAssociative mapType = ZenType.ANYMAP;
        if(keys.length>0 && values.length>0) mapType = new ZenTypeAssociative(values[0].getType(),keys[0].getType());
        return predictMapType(map,mapType);
    }

    private Expression getMemberExpression(ParsedExpressionMember member) {
        ParsedExpressionMemberAccessor access = (ParsedExpressionMemberAccessor)member;
        return getExpression(access.getValue());
    }

    public ZenType getType() {
        return this.expression.getType();
    }

    private Expression makeNullExpression() {
        return new ExpressionString(null,"null");
    }

    private Expression predict(ParsedExpression parsed, ZenType predictedType) {
        if(parsed instanceof ParsedExpressionArray)
            return predictArrayType((ParsedExpressionArray)parsed,predictedType);
        if(parsed instanceof ParsedExpressionMap)
            return predictMapType((ParsedExpressionMap)parsed,predictedType);
        return getExpression(parsed);
    }

    private Expression predictArrayType(ParsedExpressionArray array, ZenType predictedType) {
        ZenTypeArrayBasic arrayType = ZenType.ANYARRAY;
        ICastingRule castingRule = null;
        if(predictedType instanceof ZenTypeArray)
            if(predictedType instanceof ZenTypeArrayBasic)
                arrayType =(ZenTypeArrayBasic) predictedType;
        else {
            castingRule = ZenType.ANYARRAY.getCastingRule(predictedType, environment);
            if(Objects.nonNull(castingRule)) {
                if(castingRule.getInputType() instanceof ZenTypeArray) {
                    if(castingRule.getInputType() instanceof ZenTypeArrayBasic)
                        arrayType = (ZenTypeArrayBasic) castingRule.getInputType();
                } else {
                    this.environment.error(array.getPosition(), "Invalid caster - any[] caster but input "+
                            "type is not an array");
                    castingRule = null;
                }
            }
        }
        Expression[] contents = getExpressionArray(((ParsedExpressionArrayAccessor)array).getContents());
        Expression result = new ExpressionArray(array.getPosition(),arrayType,contents);
        return Misc.getNullable(castingRule,new ExpressionAs(array.getPosition(),result,castingRule),result);
    }

    private Expression predictMapType(ParsedExpressionMap map, ZenType predictedType) {
        ICastingRule castingRule = null;
        ZenTypeAssociative mapType = ZenType.ANYMAP;
        if(predictedType instanceof ZenTypeAssociative) mapType = (ZenTypeAssociative)predictedType;
        else {
            castingRule = ZenType.ANYMAP.getCastingRule(predictedType,this.environment);
            if(castingRule != null) {
                if(castingRule.getInputType() instanceof ZenTypeAssociative)
                    mapType = (ZenTypeAssociative) castingRule.getInputType();
                else this.environment.error(map.getPosition(),"Caster found for any[any] but its input is not "+
                            "an associative array");
            }
        }
        ParsedExpressionMapAccessor access = (ParsedExpressionMapAccessor)map;
        Expression[] keys = getExpressionArray(access.getKeys());
        Expression[] values = getExpressionArray(access.getValues());
        Expression result = new ExpressionMap(map.getPosition(),keys,values,mapType);
        return Misc.getNullable(castingRule,new ExpressionAs(map.getPosition(),result,castingRule),result);
    }

    private Expression testPartial(IPartialExpression partial) {
        if(partial instanceof Expression) return checkCast((Expression)partial);
        return makeNullExpression();
    }

    @Override
    public String toString() {
        List<String> lines = new ArrayList<>();
        copy(lines);
        return TextUtil.listToString(lines,"");
    }
}
